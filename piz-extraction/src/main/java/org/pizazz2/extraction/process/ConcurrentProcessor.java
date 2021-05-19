package org.pizazz2.extraction.process;

import org.apache.tika.mime.MediaType;
import org.pizazz2.ICloseable;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 并发处理器
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class ConcurrentProcessor implements ICloseable {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentProcessor.class);

    private final ExtractProcessor processor;
    private final ForkJoinPool pool;
    private BiFunction<TupleObject, MediaType, TupleObject> modify;

    public ConcurrentProcessor(ExtractProcessor processor, int parallelism) {
        this.processor = processor;
        this.pool = parallelism <= 0 ? new ForkJoinPool() : new ForkJoinPool(parallelism);
    }

    public void executeBatch(Collection<ExtractObject> objectList, TupleObject config) {
        pool.invoke(new Partition(objectList, config));
    }

    public void execute(ExtractObject object, TupleObject config) {
        pool.invoke(new Partition(Collections.singletonList(object), config));
    }

    public void setModify(BiFunction<TupleObject, MediaType, TupleObject> modify) {
        this.modify = modify;
    }

    @Override
    public void destroy(Duration timeout) {
        ThreadUtils.shutdown(pool, timeout);
    }

    private class Partition extends RecursiveAction {
        private static final long serialVersionUID = -1L;
        private final Collection<ExtractObject> objectList;
        private final TupleObject config;

        public Partition(Collection<ExtractObject> objectList, TupleObject config) {
            this.objectList = objectList;
            this.config = config;
        }

        @Override
        protected void compute() {
            Collection<ExtractObject> collection = null;
            // 当碎片为1 时开始处理
            if (objectList.size() == NumberUtils.ONE.intValue()) {
                ExtractObject tmp = objectList.iterator().next();
                try {
                    processor.extract(tmp, config, modify);
                } catch (DetectionException | ParseException e) {
                    logger.error(e.getMessage());
                    return;
                }
                if (tmp.hasAttachment()) {
                    collection = tmp.getAttachment();
                }
            } else {
                collection = objectList;
            }
            if (!CollectionUtils.isEmpty(collection)) {
                // 需要先统一完成fork再进行join
                collection.stream().map(item -> new Partition(Collections.singletonList(item), config).fork())
                        .collect(Collectors.toList()).forEach(ForkJoinTask::join);
            }
        }
    }
}

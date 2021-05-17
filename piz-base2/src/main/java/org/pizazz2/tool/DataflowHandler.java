package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.PizContext;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.data.TupleObject;
import org.pizazz2.tool.ref.IDataflowListener;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * 流式处理操作
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class DataflowHandler<T extends LinkedObject<byte[]>> implements ICloseable {
    protected final BiConsumer<Collection<T>, TupleObject> consumer;
    protected final IDataflowListener<T> listener;
    protected final TupleObject config;
    protected final boolean sync;
    protected final Semaphore semaphore;
    protected final ThreadPoolExecutor executor;

    public DataflowHandler(BiConsumer<Collection<T>, TupleObject> consumer, IDataflowListener<T> listener,
                           TupleObject config, boolean sync, int threads) {
        this.consumer = consumer;
        this.listener = listener;
        this.config = config;
        this.sync = sync;
        int tmp = Math.max(threads, 1);
        this.semaphore = new Semaphore(tmp);
        executor = new ThreadPoolExecutor(tmp, tmp, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new PizThreadFactory(PizContext.NAMING_SHORT +
                "_dataflow_execute_", true));
    }

    public void execute(long executionId, List<T> data) {
        Runnable toRelease = () -> {};
        boolean bulkRequestSetupSuccessful = false;
        try {
            listener.before(executionId,data);
            // 只允许单一处理进行通过
            semaphore.acquire();
            toRelease = semaphore::release;
            Runnable runnable = () -> {
                try {
                    consumer.accept(data, config);
                    listener.after(executionId, data);
                } catch (RuntimeException e) {
                    listener.exception(executionId, data, e);
                } finally {
                    semaphore.release();
                }
            };
            // 同步执行
            if (sync) {
                runnable.run();
            } else {
                executor.execute(runnable);
            }
            bulkRequestSetupSuccessful = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.exception(executionId, data, e);
        } catch (Exception e) {
            listener.exception(executionId, data, e);
        } finally {
            if (!bulkRequestSetupSuccessful) {
                toRelease.run();
            }
        }
    }

    @Override
    public void destroy(Duration timeout) {
        if (timeout != null && !timeout.isZero()) {
            executor.shutdown();
        } else {
            executor.shutdownNow();
        }
    }
}

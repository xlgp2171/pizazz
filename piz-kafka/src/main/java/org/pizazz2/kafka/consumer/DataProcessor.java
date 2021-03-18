package org.pizazz2.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.pizazz2.ICloseable;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.consumer.adapter.IBridge;
import org.pizazz2.kafka.consumer.adapter.IProcessAdapter;
import org.pizazz2.kafka.consumer.adapter.SequenceAdapter;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * 数据处理组件
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class DataProcessor<K, V> extends AbstractClassPlugin<TupleObject> implements ICloseable {
    private final Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    private final IOffsetProcessor offset;
    private final ConsumerIgnoreEnum ignore;
    private IProcessAdapter adapter;

    public DataProcessor(IOffsetProcessor offset, ConsumerModeEnum mode, ConsumerIgnoreEnum ignore, TupleObject config) throws BaseException {
        super(config);
        this.offset = offset;
        this.ignore = ignore;
        adapter = loadAdapter(mode);
    }

    protected IProcessAdapter loadAdapter(ConsumerModeEnum mode) throws BaseException {
        adapter = cast(loadPlugin("classpath", new SequenceAdapter(), null, true), IProcessAdapter.class);
        adapter.setMode(mode);
        logger.info("subscription data processor initialized,config=" + getConfig());
        return adapter;
    }

    public Map<String, Object> optimizeKafkaConfig(Map<String, Object> kafkaConfig) {
        return kafkaConfig;
    }

    public void consumeReady(KafkaConsumer<K, V> consumer, IDataExecutor<K, V> executor, boolean hasRecord) {
        executor.begin(hasRecord);
    }

    public void consume(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record, IDataExecutor<K, V> executor) throws KafkaException {
        adapter.accept(new IBridge() {
            @Override
            public String getId() {
                return StringUtils.join(new Object[] { record.topic(), record.partition(), record.offset(), record.timestamp() },
						KafkaConstant.SEPARATOR);
            }

            @Override
            public void passing() throws Exception {
                // 消费数据
                executor.execute(record);
                // 处理偏移量
                offset.each(consumer, record);
            }
        }, ignore);
    }

    public void consumeComplete(KafkaConsumer<K, V> consumer, IDataExecutor<K, V> executor, KafkaException e) throws KafkaException {
        if (e != null) {
            executor.throwException(e);
        } else {
            executor.end(offset);
        }
        offset.complete(consumer, e);
    }

    public String report() {
        return adapter.report();
    }

    @Override
    protected void log(String message, BaseException exception) {
        if (exception != null) {
            logger.error(message, exception);
        } else if (KafkaConstant.DEBUG_MODE) {
            logger.debug(message);
        }
    }

    @Override
    public void destroy(Duration timeout) {
        unloadPlugin(adapter, timeout);
        logger.info("subscription data processor destroyed,timeout=" + timeout);
    }
}

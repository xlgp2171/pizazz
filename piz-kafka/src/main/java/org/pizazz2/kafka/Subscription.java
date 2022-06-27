package org.pizazz2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.consumer.*;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.kafka.core.AbstractClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Kafka接收数据<br>
 * 支持处理:
 * <li>自动 异步提交 线程池方式处理 一轮数据
 * <li>自动 异步提交 循环处理 一轮数据
 * <li>手动 异步提交 线程池方式处理 每个数据
 * <li>手动 同步提交 线程池方式处理 每个数据
 * <li>手动 异步提交 循环处理 一轮数据
 * <li>手动 异步提交 循环处理 每个数据
 * <li>手动 同步提交 循环处理 一轮数据
 * <li>手动 同步提交 循环处理 每个数据
 * TODO 需要参考Springboot插件的实现
 *
 * @param <K> 消息Key
 * @param <V> 消息Value
 * @author xlgp2171
 * @version 2.1.220626
 */
public class Subscription<K, V> extends AbstractClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);

    private final Lock lock = new ReentrantLock(true);
    private final AtomicBoolean loop = new AtomicBoolean(true);
    private KafkaConsumer<K, V> consumer;
    protected IOffsetProcessor offset;
    protected DataProcessor<K, V> processor;

    public Subscription(TupleObject configure) throws ValidateException, BaseException {
        super(configure);
    }

    @Override
    protected void initialize() throws BaseException, IllegalException {
        // 创建Offset处理类
        updateConfig(getConvertor().offsetProcessorConfig());
        offset = super.loadPlugin("classpath", IOffsetProcessor.class, null, true);
        offset.set(getConvertor().consumerModeValue(), getConvertor().consumerIgnoreValue());
        // 数据处理类
        processor = new DataProcessor<>(offset, getConvertor().consumerModeValue(),
                getConvertor().consumerIgnoreValue(), getConvertor().dataProcessorConfig());
        // 创建Kafka消费类
        Map<String, Object> config = offset.optimizeKafkaConfig(getConvertor().kafkaConfig());
        consumer = new KafkaConsumer<>(processor.optimizeKafkaConfig(config));
        LOGGER.info(KafkaConstant.LOG_TAG + "subscription initialized,config=" + getConfig());
    }

    public void assign(Collection<TopicPartition> partitions, IDataRecord<K, V> impl)
            throws KafkaException {
        consumer.assign(CollectionUtils.isEmpty(partitions) ? getConvertor().assignConfig() : partitions);
        LOGGER.info(KafkaConstant.LOG_TAG + "subscription:assign");
        consume(impl);
    }

    public void subscribe(Pattern pattern, IDataRecord<K, V> impl) throws KafkaException {
        subscribe(pattern, impl, null);
    }

    public void subscribe(Pattern pattern, IDataRecord<K, V> impl, ConsumerRebalanceListener listener)
            throws KafkaException {
        if (pattern == null) {
            pattern = getConvertor().topicPatternConfig();
        }
        consumer.subscribe(pattern, offset.getRebalanceListener(consumer, listener));
        LOGGER.info(KafkaConstant.LOG_TAG + "subscription:subscribe,pattern=" + pattern);
        consume(impl);
    }

    public void subscribe(IDataRecord<K, V> executor, String... topics) throws KafkaException {
        subscribe(executor, null, topics);
    }

    public void subscribe(IDataRecord<K, V> impl, ConsumerRebalanceListener listener, String... topics)
            throws KafkaException {
        Collection<String> tmp = ArrayUtils.isEmpty(topics) ? getConvertor().topicConfig() : Arrays.asList(topics);
        consumer.subscribe(tmp, offset.getRebalanceListener(consumer, listener));
        LOGGER.info(KafkaConstant.LOG_TAG + "subscription:subscribe,topics=" + tmp);
        consume(impl);
    }

    public String getGroupId() {
        return getConvertor().getConsumerGroupId();
    }

    public void unsubscribe() {
        loop.set(false);
        lock.lock();
        try {
            consumer.unsubscribe();
        } finally {
            lock.unlock();
        }
        LOGGER.info(KafkaConstant.LOG_TAG + "subscription:unsubscribe");
    }

    protected void consume(IDataRecord<K, V> impl) throws KafkaException, ValidateException {
        ValidateUtils.notNull("consume", impl);
        // 限制实现类
        if (!(impl instanceof ISingleDataExecutor) && !(impl instanceof IMultiDataExecutor)) {
            throw new KafkaException(CodeEnum.KFK_0009, "data executor invalid");
        }
        // 强制数据一直接收
        loop.set(true);
        ConsumerRecords<K, V> records = null;

        while (loop.get() && lock.tryLock()) {
            try {
                // 按周期拉取数据
                records = consumer.poll(getConvertor().durationValue());
            } catch (Exception e) {
                if (loop.get()) {
                    if (getConvertor().consumerIgnoreValue().consumeThrowable()) {
                        loop.set(false);
                        throw new KafkaException(CodeEnum.KFK_0010, "poll data:" + getConvertor().durationValue(), e);
                    }
                    LOGGER.error(KafkaConstant.LOG_TAG + loop.get() + " pool data:" + e.getMessage(), e);
                }
            } finally {
                lock.unlock();
            }
            boolean hasRecord = records != null && !records.isEmpty();
            processor.consumeReady(consumer, impl, hasRecord ? records.count() : 0);
            try {
                if (hasRecord && loop.get()) {
                    TopicPartition partition = impl.topicPartitionFilter();
                    // 是否过滤
                    if (partition != null) {
                        Collection<ConsumerRecord<K, V>> result = records.records(partition);
                        // 单一或多个数据接收方式
                        if (impl instanceof ISingleDataExecutor) {
                            for (ConsumerRecord<K, V> item : result) {
                                processor.consume(consumer, item, (ISingleDataExecutor<K, V>) impl);
                            }
                        } else {
                            processor.consume(consumer, result, (IMultiDataExecutor<K, V>) impl);
                        }
                    } else {
                        // 单一或多个数据接收方式
                        if (impl instanceof ISingleDataExecutor) {
                            for (ConsumerRecord<K, V> item : records) {
                                processor.consume(consumer, item, (ISingleDataExecutor<K, V>) impl);
                            }
                        } else {
                            Collection<ConsumerRecord<K, V>> result = new LinkedList<>();
                            records.forEach(result::add);
                            processor.consume(consumer, result, (IMultiDataExecutor<K, V>) impl);
                        }
                    }
                }
                processor.consumeComplete(consumer, impl, null);
            } catch (KafkaException e) {
                processor.consumeComplete(consumer, impl, e);
                LOGGER.error(KafkaConstant.LOG_TAG + "consume data:" + e.getMessage(), e);
                throw e;
            }
        }
    }

    public KafkaConsumer<K, V> getTarget() {
        return consumer;
    }

    @Override
    public void destroy(Duration timeout) {
        if (consumer != null && loop.compareAndSet(true, false)) {
            consumer.wakeup();
            try {
                if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    unsubscribe();
                }
            } catch (InterruptedException e) {
                // do nothing
            } finally {
                lock.unlock();
            }
            super.destroy(timeout);
            SystemUtils.destroy(processor, timeout);
            unloadPlugin(offset, timeout);
            consumer.close(timeout);
            LOGGER.info(KafkaConstant.LOG_TAG + "subscription destroyed,timeout=" + timeout);
        }
    }
}

package org.pizazz2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.consumer.DataProcessor;
import org.pizazz2.kafka.consumer.IDataExecutor;
import org.pizazz2.kafka.consumer.IOffsetProcessor;
import org.pizazz2.kafka.consumer.OffsetProcessor;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.kafka.support.AbstractClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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
 *
 * @param <K> 消息Key
 * @param <V> 消息Value
 * @author xlgp2171
 * @version 2.0.210301
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
    protected void setUpConfig() throws BaseException {
        super.setUpConfig();
        // 创建Offset处理类
        updateConfig(getConvertor().offsetProcessorConfig());
        offset = cast(loadPlugin("classpath", new OffsetProcessor(), null, true), IOffsetProcessor.class);
        offset.set(getConvertor().consumerModeValue(), getConvertor().consumerIgnoreValue());
        // 数据处理类
        processor = new DataProcessor<>(offset, getConvertor().consumerModeValue(), getConvertor().consumerIgnoreValue(), getConvertor().dataProcessorConfig());
        // 创建Kafka消费类
        Map<String, Object> config = offset.optimizeKafkaConfig(getConvertor().kafkaConfig());
        consumer = new KafkaConsumer<>(processor.optimizeKafkaConfig(config));
        LOGGER.info("subscription initialized,config=" + getConfig());
    }

    public void assign(Collection<TopicPartition> partitions, IDataExecutor<K, V> executor) throws KafkaException {
        consumer.assign(CollectionUtils.isEmpty(partitions) ? getConvertor().assignConfig() : partitions);
        LOGGER.info("subscription:assign");
        consume(executor);
    }

    public void subscribe(Pattern pattern, IDataExecutor<K, V> executor) throws KafkaException {
        subscribe(pattern, executor, null);
    }

    public void subscribe(Pattern pattern, IDataExecutor<K, V> executor, ConsumerRebalanceListener listener) throws KafkaException {
        if (pattern == null) {
            pattern = getConvertor().topicPatternConfig();
        }
        consumer.subscribe(pattern, offset.getRebalanceListener(consumer, listener));
        LOGGER.info("subscription:subscribe,pattern=" + pattern);
        consume(executor);
    }

    public void subscribe(IDataExecutor<K, V> executor, String... topics) throws KafkaException {
        subscribe(executor, null, topics);
    }

    public void subscribe(IDataExecutor<K, V> executor, ConsumerRebalanceListener listener, String... topics) throws KafkaException {
        Collection<String> tmp = ArrayUtils.isEmpty(topics) ? getConvertor().topicConfig() : Arrays.asList(topics);
        consumer.subscribe(tmp, offset.getRebalanceListener(consumer, listener));
        LOGGER.info("subscription:subscribe,topics=" + tmp);
        consume(executor);
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
        LOGGER.info("subscription:unsubscribe");
    }

    protected void consume(IDataExecutor<K, V> executor) throws KafkaException {
        if (executor == null) {
            throw new KafkaException(CodeEnum.KFK_0009, "data executor null");
        }
        loop.set(true);
        ConsumerRecords<K, V> records = null;

        while (loop.get() && lock.tryLock()) {
            try {
                records = consumer.poll(getConvertor().durationValue());
            } catch (Exception e) {
                if (loop.get()) {
                    if (getConvertor().consumerIgnoreValue().consumeThrowable()) {
                        throw new KafkaException(CodeEnum.KFK_0010, "poll data:" + getConvertor().durationValue(), e);
                    }
                    LOGGER.error(loop.get() + " pool data:" + e.getMessage(), e);
                }
            } finally {
                lock.unlock();
            }
            processor.consumeReady(consumer, executor, records == null || records.isEmpty());
            try {
                if (records != null && !records.isEmpty() && loop.get()) {
                    for (ConsumerRecord<K, V> item : records) {
                        processor.consume(consumer, item, executor);
                    }
                }
				processor.consumeComplete(consumer, executor, null);
            } catch (KafkaException e) {
				processor.consumeComplete(consumer, executor, e);
                LOGGER.error("consume data:" + e.getMessage(), e);
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
            LOGGER.info("subscription destroyed,timeout=" + timeout);
        }
    }
}

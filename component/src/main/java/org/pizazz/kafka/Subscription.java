package org.pizazz.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.kafka.consumer.DataProcessor;
import org.pizazz.kafka.consumer.IDataExecutor;
import org.pizazz.kafka.consumer.IOffsetProcessor;
import org.pizazz.kafka.consumer.OffsetProcessor;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaError;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.kafka.support.AbstractClient;
import org.pizazz.message.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author xlgp2171
 *
 * @param <K>
 * @param <V>
 */
public class Subscription<K, V> extends AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);

	private final Lock lock = new ReentrantLock();
	private final AtomicBoolean loop = new AtomicBoolean(true);
	private KafkaConsumer<K, V> consumer;
	protected IOffsetProcessor offset;
	protected DataProcessor<K, V> processor;

	@Override
	public void initialize(TupleObject config) throws AssertException, UtilityException, ToolException, KafkaException  {
		super.initialize(config);
		// 创建Offset处理类
		updateConfig(getConvertor().offsetProcessorConfig());
		offset = cast(loadPlugin("classpath", new OffsetProcessor(), null, true), IOffsetProcessor.class);
		offset.set(getConvertor().consumerModeValue(), getConvertor().consumerIgnoreValue());
		// 数据处理类
		processor = new DataProcessor<K, V>(offset, getConvertor().consumerModeValue(),
				getConvertor().consumerIgnoreValue());
		processor.initialize(getConvertor().dataProcessorConfig());
		// 创建Kafka消费类
		Map<String, Object> _config = offset.optimizeKafkaConfig(getConvertor().kafkaConfig());
		consumer = new KafkaConsumer<K, V>(processor.optimizeKafkaConfig(_config));
		LOGGER.info("subscription initialized,config=" + config);
	}

	public void assign(Collection<TopicPartition> partitions, IDataExecutor<K, V> executor) throws KafkaException {
		getConsumer().assign(CollectionUtils.isEmpty(partitions) ? getConvertor().assignConfig() : partitions);
		LOGGER.info("subscription:assign");
		consume(executor);
	}

	public void subscribe(Pattern pattern, IDataExecutor<K, V> executor) throws KafkaException {
		subscribe(pattern, executor, null);
	}

	public void subscribe(Pattern pattern, IDataExecutor<K, V> executor, ConsumerRebalanceListener listener)
			throws KafkaException {
		Pattern _pattern = pattern == null ? getConvertor().topicPatternConfig() : pattern;
		getConsumer().subscribe(_pattern, offset.getRebalanceListener(getConsumer(), listener));
		LOGGER.info("subscription:subscribe,pattern=" + _pattern);
		consume(executor);
	}

	public void subscribe(IDataExecutor<K, V> executor, String... topics) throws KafkaException {
		subscribe(executor, null, topics);
	}

	public void subscribe(IDataExecutor<K, V> executor, ConsumerRebalanceListener listener, String... topics)
			throws KafkaException {
		Collection<String> _topics = ArrayUtils.isEmpty(topics) ? getConvertor().topicConfig() : Arrays.asList(topics);
		getConsumer().subscribe(_topics, offset.getRebalanceListener(getConsumer(), listener));
		LOGGER.info("subscription:subscribe,topics=" + _topics);
		consume(executor);
	}

	public String getGroupId() {
		String _tmp = StringUtils.EMPTY;

		if (getConvertor() != null) {
			_tmp = getConvertor().getConsumerGroupId();
		}
		return _tmp;
	}

	public void unsubscribe() {
		getConsumer().unsubscribe();
		LOGGER.info("subscription:unsubscribe");
	}

	protected void consume(IDataExecutor<K, V> executor) throws KafkaException {
		if (executor == null) {
			throw new KafkaException(CodeEnum.KFK_0009, "data executor null");
		}
		ConsumerRecords<K, V> _records = null;

		while (loop.get() && lock.tryLock()) {
			try {
				_records = getConsumer().poll(getConvertor().durationValue());
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
			if (_records == null || _records.isEmpty() || !loop.get()) {
				continue;
			}
			processor.consumeReady(getConsumer(), executor);
			try {
				for (ConsumerRecord<K, V> _item : _records) {
					processor.consume(getConsumer(), _item, executor);
				}
				processor.consumeComplete(getConsumer(), executor, null);
			} catch (KafkaException e) {
				processor.consumeComplete(getConsumer(), executor, e);
				LOGGER.error("consume data:" + e.getMessage(), e);
				throw e;
			}
		}
	}

	protected KafkaConsumer<K, V> getConsumer() {
		if (consumer == null) {
			throw new KafkaError(ErrorCodeEnum.ERR_0005, "consumer not initialize");
		}
		return consumer;
	}

	@Override
	public void destroy(Duration timeout) {
		if (consumer != null && isInitialize() && loop.compareAndSet(true, false)) {
			loop.get();
			consumer.wakeup();
			try {
				lock.tryLock(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			} finally {
				lock.unlock();
			}
			unsubscribe();
			super.destroy(timeout);
			SystemUtils.destroy(processor, timeout);
			unloadPlugin(offset, timeout);
			IOUtils.close(consumer);
			LOGGER.info("subscription destroyed,timeout=" + timeout);
		}
	}
}

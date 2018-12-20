package org.pizazz.kafka;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.consumer.DataProcessor;
import org.pizazz.kafka.consumer.IDataExecutor;
import org.pizazz.kafka.consumer.IOffsetProcessor;
import org.pizazz.kafka.consumer.OffsetProcessor;
import org.pizazz.kafka.exception.KafkaError;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.kafka.ref.AbstractClient;
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

	private boolean loop = true;
	private KafkaConsumer<K, V> consumer;
	protected IOffsetProcessor offset;
	protected DataProcessor<K, V> processor;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		super.initialize(config);

		// 创建Offset处理类
		updateConfig(getConvertor().offsetProcessorConfig());
		offset = cast(loadPlugin("classpath", new OffsetProcessor(), null, true), IOffsetProcessor.class);
		offset.setMode(getConvertor().modeValue());
		// 数据处理类
		processor = new DataProcessor<K, V>(offset, getConvertor().modeValue());
		processor.initialize(getConvertor().dataProcessorConfig());
		// 创建Kafka消费类
		consumer = new KafkaConsumer<K, V>(offset.optimizeKafkaConfig(getConvertor().kafkaConfig()));
		LOGGER.info("subscription initialized,config=" + config);
	}

	public void assign() throws KafkaException {
		getConsumer().assign(getConvertor().assignConfig());
		LOGGER.info("subscription:assign");
	}

	public void subscribeByPattern(ConsumerRebalanceListener listener) throws KafkaException {
		getConsumer().subscribe(getConvertor().topicPatternConfig(),
				offset.getRebalanceListener(getConsumer(), listener));
		LOGGER.info("subscription:subscribe,pattern=" + getConvertor().topicPatternConfig());
	}

	public void subscribeByTopics(ConsumerRebalanceListener listener) throws KafkaException {
		getConsumer().subscribe(getConvertor().topicConfig(), offset.getRebalanceListener(getConsumer(), listener));
		LOGGER.info("subscription:subscribe,topics=" + CollectionUtils.toString(getConvertor().topicConfig()));
	}

	public void unsubscribe() {
		getConsumer().unsubscribe();
		LOGGER.info("subscription:unsubscribe");
	}

	public void consume(IDataExecutor<K, V> executor) throws KafkaException {
		LOGGER.info("wating for consume record");

		while (loop) {
			ConsumerRecords<K, V> _records = getConsumer().poll(getConvertor().durationValue());
			try {
				for (ConsumerRecord<K, V> _item : _records) {
					processor.consume(getConsumer(), _item, executor);
				}
				processor.consumeComplete(getConsumer(), null);
			} catch (KafkaException e) {
				processor.consumeComplete(getConsumer(), e);
				LOGGER.error("consume error:" + e.getMessage(), e);
				throw e;
			}
		}
	}

	@Override
	protected void log(String msg, BaseException e) {
		if (e != null) {
			LOGGER.error(msg, e);
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);
		}
	}

	protected KafkaConsumer<K, V> getConsumer() {
		if (consumer == null) {
			throw new KafkaError(ErrorCodeEnum.ERR_0005, "consumer not initialize");
		}
		return consumer;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (consumer != null && isInitialize()) {
			unsubscribe();
			loop = false;
			consumer.wakeup();
			super.destroy(timeout);
			SystemUtils.destroy(processor, timeout);
			SystemUtils.destroy(offset, timeout);
			IOUtils.close(consumer);
			LOGGER.info("subscription destroyed,timeout=" + timeout);
		}
	}
}

package org.pizazz.kafka.consumer;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.KafkaConstant;
import org.pizazz.kafka.consumer.adapter.Bridge;
import org.pizazz.kafka.consumer.adapter.IProcessAdapter;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor<K, V> extends AbstractClassPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessor.class);

	private final IOffsetProcessor offset;
	private final ConsumerModeEnum mode;
	private IProcessAdapter adapter;

	public DataProcessor(IOffsetProcessor offset, ConsumerModeEnum mode) {
		this.offset = offset;
		this.mode = mode;
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
		setConfig(config);
		adapter = cast(loadPlugin("classpath", null, null, true), IProcessAdapter.class);
		try {
			adapter.set(mode);
		} catch (KafkaException e) {
			throw new BaseException(BasicCodeEnum.MSG_0005, e.getMessage(), e);
		}
		LOGGER.info("subscription data processor initialized,config=" + config);
	}

	public void consume(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record, IDataExecutor<K, V> executor)
			throws KafkaException {
		adapter.accept(new Bridge() {
			@Override
			public String getId() {
				return new StringBuilder(record.topic()).append(KafkaConstant.SEPARATOR).append(record.partition())
						.append(KafkaConstant.SEPARATOR).append(record.offset()).append(KafkaConstant.SEPARATOR)
						.append(record.timestamp()).toString();
			}

			@Override
			public void passing() throws Exception {
				executor.execute(record);
				offset.each(consumer, record);
			}
		});
	}

	public void consumeComplete(KafkaConsumer<K, V> consumer, KafkaException e) {
		offset.complete(consumer, e);
	}

	@Override
	protected void log(String msg, BaseException e) {
		if (e != null) {
			LOGGER.error(msg, e);
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);
		}
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		SystemUtils.destroy(adapter, timeout);
		LOGGER.info("subscription data processor destroyed,timeout=" + timeout);
	}
}

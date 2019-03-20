package org.pizazz.kafka.consumer;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.kafka.KafkaConstant;
import org.pizazz.kafka.consumer.adapter.IBridge;
import org.pizazz.kafka.consumer.adapter.IProcessAdapter;
import org.pizazz.kafka.consumer.adapter.SequenceAdapter;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor<K, V> extends AbstractClassPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessor.class);

	private final IOffsetProcessor offset;
	private final ConsumerModeEnum mode;
	private final ConsumerIgnoreEnum ignore;
	private IProcessAdapter adapter;

	public DataProcessor(IOffsetProcessor offset, ConsumerModeEnum mode, ConsumerIgnoreEnum ignore) {
		this.offset = offset;
		this.mode = mode;
		this.ignore = ignore;
	}

	@Override
	public void initialize(TupleObject config) throws KafkaException, AssertException, UtilityException, ToolException {
		setConfig(config);
		adapter = cast(loadPlugin("classpath", new SequenceAdapter(), null, true), IProcessAdapter.class);
		adapter.set(mode);
		LOGGER.info("subscription data processor initialized,config=" + config);
	}

	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> kafkaConfig) {
		return kafkaConfig;
	}

	public void consumeReady(KafkaConsumer<K, V> consumer, IDataExecutor<K, V> executor) {
		executor.begin();
	}

	public void consume(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record, IDataExecutor<K, V> executor)
			throws KafkaException {
		adapter.accept(new IBridge() {
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
		}, ignore);
	}

	public void consumeComplete(KafkaConsumer<K, V> consumer, IDataExecutor<K, V> executor, KafkaException e)
			throws KafkaException {
		if (e != null) {
			executor.throwException(e);
		} else {
			executor.end(offset);
		}
		offset.complete(consumer, e);
	}

	public String monitor() {
		return adapter.monitor();
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
	public void destroy(Duration timeout) {
		unloadPlugin(adapter, timeout);
		LOGGER.info("subscription data processor destroyed,timeout=" + timeout);
	}
}

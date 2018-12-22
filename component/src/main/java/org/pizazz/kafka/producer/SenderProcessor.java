package org.pizazz.kafka.producer;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.pizazz.IPlugin;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenderProcessor<K, V> implements IPlugin {
	private final static Logger LOGGER = LoggerFactory.getLogger(SenderProcessor.class);
	private final ProducerModeEnum mode;

	public SenderProcessor(ProducerModeEnum mode) {
		this.mode = mode;
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	public Future<RecordMetadata> sentData(KafkaProducer<K, V> producer, ProducerRecord<K, V> record, Callback callback)
			throws KafkaException {
		Future<RecordMetadata> _tmp;
		try {
			_tmp = producer.send(record, new ProxyCallback(callback));
		} catch (Exception e) {
			throw new KafkaException(CodeEnum.KFK_0012, "data send:" + e.getMessage(), e);
		}
		if (mode.isSync()) {
			try {
				_tmp.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new KafkaException(CodeEnum.KFK_0012, "data send:" + e.getMessage(), e);
			}
		}
		return _tmp;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
	}

	private static class ProxyCallback implements Callback {
		private final Callback callback;

		public ProxyCallback(Callback callback) {
			this.callback = callback;
		}

		@Override
		public void onCompletion(RecordMetadata metadata, Exception e) {
			if (e != null) {
				LOGGER.error("data send:" + metadata, e);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("data send:" + metadata);
			}
			if (callback != null) {
				callback.onCompletion(metadata, e);
			}
		}
	}
}

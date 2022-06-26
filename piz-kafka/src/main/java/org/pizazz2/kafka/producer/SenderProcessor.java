package org.pizazz2.kafka.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.pizazz2.data.TupleObject;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 发送消息处理组件
 *
 * @author xlgp2171
 * @version 2.0.220625
 */
public class SenderProcessor<K, V> {
	private final Logger logger = LoggerFactory.getLogger(SenderProcessor.class);
	private final ProducerModeEnum mode;

	public SenderProcessor(ProducerModeEnum mode, TupleObject config) {
		this.mode = mode;
	}

	public Future<RecordMetadata> sentData(KafkaProducer<K, V> producer, ProducerRecord<K, V> record, Callback callback)
			throws KafkaException {
		Future<RecordMetadata> tmp;
		try {
			tmp = producer.send(record, new ProxyCallback(callback));
		} catch (Exception e) {
			throw new KafkaException(CodeEnum.KFK_0012, "data send:" + e.getMessage(), e);
		}
		// 是否同步模式
		if (mode.isSync()) {
			try {
				tmp.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new KafkaException(CodeEnum.KFK_0012, "data send:" + e.getMessage(), e);
			}
		}
		return tmp;
	}

	/**
	 * 发送代理回调
	 */
	private class ProxyCallback implements Callback {
		private final Callback callback;

		public ProxyCallback(Callback callback) {
			this.callback = callback;
		}

		@Override
		public void onCompletion(RecordMetadata metadata, Exception e) {
			if (e != null) {
				logger.error(KafkaConstant.LOG_TAG + "data send:" + metadata, e);
			} else if (KafkaConstant.DEBUG_MODE) {
				logger.debug(KafkaConstant.LOG_TAG + "data send:" + metadata);
			}
			if (callback != null) {
				callback.onCompletion(metadata, e);
			}
		}
	}
}

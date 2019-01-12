package org.pizazz.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.common.IOUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.KafkaError;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.kafka.producer.ITransactionProcessor;
import org.pizazz.kafka.producer.SenderProcessor;
import org.pizazz.kafka.producer.TransactionProcessor;
import org.pizazz.kafka.ref.AbstractClient;
import org.pizazz.message.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Production<K, V> extends AbstractClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(Production.class);

	private KafkaProducer<K, V> producer;
	protected ITransactionProcessor transaction;
	protected SenderProcessor<K, V> processor;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		super.initialize(config);
		//
		updateConfig(getConvertor().transactionProcessorConfig());
		transaction = cast(loadPlugin("classpath", new TransactionProcessor(), null, true),
				ITransactionProcessor.class);
		transaction.set(getConvertor().producerModeValue());
		//
		processor = new SenderProcessor<K, V>(getConvertor().producerModeValue());
		processor.initialize(getConvertor().senderProcessorConfig());
		//
		producer = new KafkaProducer<K, V>(transaction.optimizeKafkaConfig(getConvertor().kafkaConfig()));
		try {
			transaction.initTransactions(producer);
		} catch (KafkaException e) {
			throw new KafkaError(ErrorCodeEnum.ERR_0002, e);
		}
		LOGGER.info("production initialized,config=" + config);
	}

	public Production<K, V> beginTransaction() throws KafkaException {
		try {
		transaction.beginTransaction(getProducer());
		} catch(KafkaException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		return this;
	}

	public Production<K, V> commitTransaction() throws KafkaException {
		return commitTransaction(null, null);
	}

	public Production<K, V> commitTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String groupId)
			throws KafkaException {
		try {
		transaction.commitTransaction(getProducer(), offsets, groupId);
	} catch(KafkaException e) {
		LOGGER.error(e.getMessage(), e);
		throw e;
	}
		return this;
	}

	public void abortTransaction() throws KafkaException {
		try {
		transaction.abortTransaction(getProducer());
	} catch(KafkaException e) {
		LOGGER.error(e.getMessage(), e);
		throw e;
	}
	}

	public Future<RecordMetadata> sent(ProducerRecord<K, V> record) throws KafkaException {
		return sent(record, null);
	}

	public Future<RecordMetadata> sent(ProducerRecord<K, V> record, Callback callback) throws KafkaException {
		return processor.sentData(getProducer(), record, callback);
	}

	public void flush() {
		getProducer().flush();
	}

	protected KafkaProducer<K, V> getProducer() {
		if (producer == null) {
			throw new KafkaError(ErrorCodeEnum.ERR_0005, "producer not initialize");
		}
		return producer;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (producer != null && isInitialize()) {
			flush();
			super.destroy(timeout);
			SystemUtils.destroy(processor, timeout);
			unloadPlugin(transaction, timeout);
			IOUtils.close(producer);
			LOGGER.info("production destroyed,timeout=" + timeout);
		}
	}
}

package org.pizazz.kafka.producer;

import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.IPlugin;
import org.pizazz.kafka.exception.KafkaException;

public interface ITransactionProcessor extends IPlugin {

	public <K, V> void initTransactions(KafkaProducer<K, V> producer) throws KafkaException;

	public <K, V> void beginTransaction(KafkaProducer<K, V> producer) throws KafkaException;

	public <K, V> void commitTransaction(KafkaProducer<K, V> producer, Map<TopicPartition, OffsetAndMetadata> offsets,
            String groupId) throws KafkaException;

	public <K, V> void abortTransaction(KafkaProducer<K, V> producer) throws KafkaException;
	
	public void set(ProducerModeEnum mode);

	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config);
}
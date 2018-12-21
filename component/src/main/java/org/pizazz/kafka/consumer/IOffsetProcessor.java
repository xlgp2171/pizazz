package org.pizazz.kafka.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.IPlugin;
import org.pizazz.kafka.exception.KafkaException;

public interface IOffsetProcessor extends IPlugin {

	public <K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record) throws KafkaException;

	public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) throws KafkaException;

	public void set(ConsumerModeEnum mode, ConsumerIgnoreEnum ignore);

	public Map<TopicPartition, OffsetAndMetadata> getOffsetCache();

	public void restOffsetCommitted();

	public <K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer,
			ConsumerRebalanceListener listener);

	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config);
}

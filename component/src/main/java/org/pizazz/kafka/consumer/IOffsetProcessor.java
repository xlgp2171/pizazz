package org.pizazz.kafka.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.pizazz.IPlugin;
import org.pizazz.kafka.exception.KafkaException;

public interface IOffsetProcessor extends IPlugin {

	public <K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record);

	public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e);

	public void setMode(ConsumerModeEnum mode);

	public void restOffsetCommitted();

	public <K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer,
			ConsumerRebalanceListener listener);

	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config);
}

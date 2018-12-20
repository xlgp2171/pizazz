package org.pizazz.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@FunctionalInterface
public interface IDataExecutor<K, V> {

	public void execute(ConsumerRecord<K, V> record) throws Exception;
}

package org.pizazz.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IDataExecutor<K, V> {
	public default void begin() {
	}

	public void execute(ConsumerRecord<K, V> record) throws Exception;

	public default void end(IOffsetProcessor offset) {
	}

	public default void throwException(Exception e) {
	}
}

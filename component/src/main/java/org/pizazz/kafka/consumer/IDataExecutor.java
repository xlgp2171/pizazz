package org.pizazz.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IDataExecutor<K, V> {
	public void begin();

	public void execute(ConsumerRecord<K, V> record) throws Exception;

	public void end(IOffsetProcessor offset);

	public void throwException(Exception e);
}

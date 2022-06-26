package org.pizazz2.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Collection;

/**
 * 数据批量消费实现接口
 *
 * @author xlgp2171
 * @version 2.1.220626
 */
public interface IMultiDataExecutor<K, V> extends IDataRecord<K, V> {

	/**
	 * 消费数据
	 * @param records 数据体
	 * @throws Exception 消费数据过程出现的异常
	 */
	void execute(Collection<ConsumerRecord<K, V>> records) throws Exception;
}

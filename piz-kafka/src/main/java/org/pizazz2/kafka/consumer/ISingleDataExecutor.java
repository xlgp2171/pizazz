package org.pizazz2.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 数据单个消费实现接口
 *
 * @author xlgp2171
 * @version 2.1.220626
 */
public interface ISingleDataExecutor<K, V> extends IDataRecord<K, V> {

	/**
	 * 消费数据
	 * @param record 数据体
	 * @throws Exception 消费数据过程出现的异常
	 */
	void execute(ConsumerRecord<K, V> record) throws Exception;
}

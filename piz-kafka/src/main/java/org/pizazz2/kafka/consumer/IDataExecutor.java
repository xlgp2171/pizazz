package org.pizazz2.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * 数据消费实现接口
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public interface IDataExecutor<K, V> {
	/**
	 * 一轮数据消费之前
	 * @param hasRecord 接收数据数量
	 */
	default void begin(boolean hasRecord) {
	}

	/**
	 * 消费数据
	 * @param record 数据体
	 * @throws Exception 消费数据过程出现的异常
	 */
	void execute(ConsumerRecord<K, V> record) throws Exception;

	/**
	 * 一轮数据消费之后
	 * @param offset 偏移量处理实现
	 */
	default void end(IOffsetProcessor offset) {
	}

	/**
	 * 消费过程中的异常抛出
	 * @param e 消费过程中的异常
	 */
	default void throwException(Exception e) {
	}
}

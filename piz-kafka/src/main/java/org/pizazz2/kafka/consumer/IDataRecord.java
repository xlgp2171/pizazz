package org.pizazz2.kafka.consumer;

import org.apache.kafka.common.TopicPartition;

/**
 * 数据消费实现接口
 *
 * @author xlgp2171
 * @version 2.1.220626
 */
public interface IDataRecord<K, V> {
	/**
	 * 一轮数据消费之前
	 * @param count 接收数据数量
	 */
	default void begin(int count) {
	}

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

	/**
	 * 根据TopicPartition过滤数据
	 * @return 需要过滤数据的TopicPartition
	 */
	default TopicPartition topicPartitionFilter() {
		return null;
	}
}

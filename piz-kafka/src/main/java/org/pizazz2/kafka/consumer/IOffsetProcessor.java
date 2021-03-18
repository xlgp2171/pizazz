package org.pizazz2.kafka.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.IPlugin;
import org.pizazz2.kafka.exception.KafkaException;

/**
 * 偏移量处理接口
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public interface IOffsetProcessor extends IPlugin {

	/**
	 * 处理每一个偏移量
	 * @param consumer kafka消费对象
	 * @param record 接收到的消息
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @throws KafkaException 偏移量提交异常
	 */
	<K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record) throws KafkaException;

	/**
	 * 一轮数据接收完成的处理
	 * @param consumer kafka消费对象
	 * @param e 消费数据过程中的异常
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @throws KafkaException 偏移量提交异常
	 */
	<K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) throws KafkaException;

	/**
	 * 设置消费模式和消费异常忽略枚举
	 * @param mode 消费模式
	 * @param ignore 忽略枚举
	 */
	void set(ConsumerModeEnum mode, ConsumerIgnoreEnum ignore);

	/**
	 * 获取当前偏移量缓存
	 * @return 当前偏移量缓存
	 */
	Map<TopicPartition, OffsetAndMetadata> getOffsetCache();

	/**
	 * 重置缓存偏移量
	 */
	void restOffsetCommitted();

	/**
	 * 获取消费平衡监听实现
	 * @param consumer kafka消费对象
	 * @param listener 消费节点平衡监听
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @return 包装后的消费平衡监听实现
	 */
	<K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer,
			ConsumerRebalanceListener listener);

	/**
	 * 优化kafka配置
	 * @param config kafka配置
	 * @return 优化后的kafka配置
	 */
	Map<String, Object> optimizeKafkaConfig(Map<String, Object> config);
}

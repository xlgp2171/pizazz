package org.pizazz2.kafka.producer;

import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.IPlugin;
import org.pizazz2.data.TupleObject;
import org.pizazz2.kafka.exception.KafkaException;

/**
 * 事务处理接口
 *
 * @author xlgp2171
 * @version 2.1.211215
 */
public interface ITransactionProcessor extends IPlugin<TupleObject> {

	/**
	 * 初始化事务
	 * @param producer kafka消息发布对象
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 */
	<K, V> void initTransactions(KafkaProducer<K, V> producer);

	/**
	 * 开始事务
	 * @param producer kafka消息发布对象
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @throws KafkaException 开始事务异常
	 */
	<K, V> void beginTransaction(KafkaProducer<K, V> producer) throws KafkaException;

	/**
	 * 提交事务
	 * @param producer kafka消息发布对象
	 * @param offsets 主题及分区偏移量
	 * @param groupId 组ID
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @throws KafkaException 提交事务异常
	 */
	<K, V> void commitTransaction(KafkaProducer<K, V> producer, Map<TopicPartition, OffsetAndMetadata> offsets,
            String groupId) throws KafkaException;

	/**
	 * 忽略事务
	 * @param producer kafka消息发布对象
	 * @param <K> 消息Key
	 * @param <V> 消息Value
	 * @throws KafkaException 忽略事务异常
	 */
	<K, V> void abortTransaction(KafkaProducer<K, V> producer) throws KafkaException;

	/**
	 * 设置发布模式
	 * @param mode 发布模式枚举
	 */
	void setMode(ProducerModeEnum mode);

	/**
	 * 优化kafka配置
	 * @param config kafka配置
	 * @return 优化后的kafka配置
	 */
	Map<String, Object> optimizeKafkaConfig(Map<String, Object> config);
}
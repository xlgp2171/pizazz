package org.pizazz2.kafka.consumer.adapter;

import org.pizazz2.IPlugin;
import org.pizazz2.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz2.kafka.consumer.ConsumerModeEnum;
import org.pizazz2.kafka.exception.KafkaException;

/**
 * 数据接收适配接口
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public interface IProcessAdapter extends IPlugin {

	/**
	 * 设置消费模式
	 * @param mode 消费模式枚举
	 * @throws KafkaException 设置模式异常
	 */
	void setMode(ConsumerModeEnum mode) throws KafkaException;

	/**
	 * 消息处理
	 * @param bridge 桥
	 * @param ignore 消费处理忽略异常
	 * @throws KafkaException 消息处理异常
	 */
	void accept(IBridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException;

	/**
	 * 适配器报告
	 * @return 适配器报告
	 */
	String report();
}

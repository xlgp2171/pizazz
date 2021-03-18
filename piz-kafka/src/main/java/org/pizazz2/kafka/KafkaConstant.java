package org.pizazz2.kafka;

import org.pizazz2.PizContext;
import org.pizazz2.common.BooleanUtils;
import org.pizazz2.common.SystemUtils;

/**
 * Kafka常量类
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class KafkaConstant {
	public static final String SEPARATOR = "#";
	/**
	 * kafka组件顶级配置KEY
	 */
	public static final String KEY_KAFKA = PizContext.NAMING_SHORT + "_kafka";
	/**
	 * kafka_client配置KEY<br>
	 * 订阅端和发布端都拥有该配置KEY
	 */
	public static final String KEY_CLIENT = "client";
	/**
	 * 组件配置KEY<br>
	 * 订阅端和发布端都拥有该配置KEY
	 */
	public static final String KEY_CONFIG = "config";
	/**
	 * 订阅端配置KEY
	 */
	public static final String KEY_SUBSCRIPTION = "subscription";
	/**
	 * 发布端配置KEY
	 */
	public static final String KEY_PRODUCTION = "production";
	/**
	 * 监控端配置KEY
	 */
	public static final String KEY_MONITORING = "monitoring";
	/**
	 * 主题分区配置KEY
	 */
	public static final String KEY_TOPIC_PARTITION = "topicPartition";
	/**
	 * 主题匹配配置KEY
	 */
	public static final String KEY_TOPIC_PATTERN = "topicPattern";
	/**
	 * 主题配置KEY
	 */
	public static final String KEY_TOPIC = "topic";
	/**
	 * 
	 */
	public static final String KEY_DURATION = "duration";
	/**
	 * 
	 */
	public static final String KEY_THREADS = "threads";
	/**
	 * 
	 */
	public static final String KEY_MODE = "mode";
	/**
	 * 
	 */
	public static final String KEY_IGNORE = "ignore";
	/**
	 * 
	 */
	public static final String KEY_OFFSET_PROCESSOR = "offsetProcessor";
	/**
	 * 
	 */
	public static final String KEY_TRANSACTION_PROCESSOR = "transactionProcessor";
	/**
	 * 
	 */
	public static final String KEY_DATA_PROCESSOR = "dataProcessor";
	/**
	 * 
	 */
	public static final String KEY_SENDER_PROCESSOR = "senderProcessor";
	/**
	 * 
	 */
	public static final String KEY_TEMPLATE = "template";

	public static final int DEF_DURATION_MAX = 60000;
	public static final int DEF_DURATION = 10000;

	public static final boolean DEBUG_MODE;

	static {
		String key = PizContext.NAMING_SHORT + ".ext.debug";
		DEBUG_MODE = BooleanUtils.toBoolean(SystemUtils.getSystemProperty(key, "false"));
	}
}

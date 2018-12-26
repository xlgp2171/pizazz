package org.pizazz.kafka;

import org.pizazz.Constant;

public class KafkaConstant {
	public static final String SEPARATOR = "#";
	/**
	 * kafka组件顶级配置KEY
	 */
	public static final String KEY_KAFKA = Constant.NAMING_SHORT + "_kafka";
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
	 * 
	 */
	public static final String KEY_PRODUCTION = "production";
	/**
	 * 
	 */
	public static final String KEY_TOPIC_PARTITION = "topicPartition";
	/**
	 * 
	 */
	public static final String KEY_TOPIC_PATTERN = "topicPattern";
	/**
	 * 
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
	
}

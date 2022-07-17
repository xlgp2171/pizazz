package org.pizazz2.kafka;

import org.pizazz2.PizContext;
import org.pizazz2.common.BooleanUtils;
import org.pizazz2.common.SystemUtils;

/**
 * Kafka常量类
 *
 * @author xlgp2171
 * @version 2.1.220625
 */
public class KafkaConstant {
	public static final String SEPARATOR = "#";
	/**
	 * kafka组件顶级配置KEY
	 */
	public static final String CONF_KAFKA = "transfer-kafka";

	public static final String LOG_TAG = "[" + KafkaConstant.CONF_KAFKA + "]";
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
	public static final String KEY_TOPIC_PARTITION = "topic-partition";
	/**
	 * 主题匹配配置KEY
	 */
	public static final String KEY_TOPIC_PATTERN = "topic-pattern";
	/**
	 * 主题配置KEY
	 */
	public static final String KEY_TOPIC = "topic";
	/**
	 * 拉取数据周期KEY
	 */
	public static final String KEY_DURATION = "duration";
	/**
	 * 并行线程数KEY
	 */
	public static final String KEY_THREADS = "threads";
	/**
	 * 模式KEY
	 */
	public static final String KEY_MODE = "mode";
	/**
	 * 消费忽略异常模式KEY
	 */
	public static final String KEY_IGNORE = "ignore";
	/**
	 * offset处理组件KEY
	 */
	public static final String KEY_OFFSET_PROCESSOR = "offset-processor";
	/**
	 * 事务实现运行组件KEY
	 */
	public static final String KEY_TRANSACTION_PROCESSOR = "transaction-processor";
	/**
	 * 数据处理组件KEY
	 */
	public static final String KEY_DATA_PROCESSOR = "data-processor";
	/**
	 * 发送数据组件KEY
	 */
	public static final String KEY_SENDER_PROCESSOR = "sender-processor";
	/**
	 * 模板KEY
	 */
	public static final String KEY_TEMPLATE = "template";
	/**
	 * 最大拉取数据周期
	 */
	public static final int DEF_DURATION_MAX = 60000;
	/**
	 * 默认拉取数据周期
	 */
	public static final int DEF_DURATION = 10000;
	/**
	 * DEBUG模式<br>
	 * 通过系统配置可设置
	 */
	public static final boolean DEBUG_MODE;

	static {
		// piz.ext.debug
		String key = PizContext.NAMING_SHORT + ".ext.debug";
		DEBUG_MODE = BooleanUtils.toBoolean(SystemUtils.getSystemProperty(key, "false"));
	}
}

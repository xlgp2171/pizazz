package org.pizazz.kafka.exception;

import org.pizazz.message.ref.IMessageCode;

public enum CodeEnum implements IMessageCode {
	
	KFK_0000("KFK0000#"),
	/** 不支持的订阅模式 */
	KFK_0001("KFK0001#"),
	/** 缺少消费模式assign配置 */
	KFK_0002("KFK0002#"),
	/** 消费模式assign配置参数错误 */
	KFK_0003("KFK0003#"),
	/** 缺少消费模式topic pattern配置 */
	KFK_0004("KFK0004#"),
	/** 缺少消费模式topic配置 */
	KFK_0005("KFK0005#"),
	/** 消费数据同步提交异常 */
	KFK_0006("KFK0006#"),
	/** 消费模式参数异常 */
	KFK_0007("KFK0007#"),
	/** 忽略模式参数异常 */
	KFK_0008("KFK0008#"),
	/** 数据接口空值 */
	KFK_0009("KFK0009#"),
	/** 订阅数据异常 */
	KFK_0010("KFK0010#");

	private String code;
	private StringBuffer message;
	private final Object lock = new Object();

	private CodeEnum(String code) {
		this.code = code;
	}
	
	@Override
	public String getPrefix() {
		return "KFK";
	}

	@Override
	public String getValue() {
		return message == null ? code : code + message.toString();
	}

	@Override
	public IMessageCode append(Object target) {
		if (message == null) {
			synchronized (lock) {
				if (message == null) {
					message = new StringBuffer();
				}
			}
		}
		message.append(target);
		return this;
	}

}

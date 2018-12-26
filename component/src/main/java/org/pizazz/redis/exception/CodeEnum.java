package org.pizazz.redis.exception;

import org.pizazz.message.ref.IMessageCode;

public enum CodeEnum implements IMessageCode {
	
	KFK_0000("RDS0000#"),
	/** 配置异常 */
	KFK_0001("RDS0001#");

	private String code;
	private StringBuffer message;
	private final Object lock = new Object();

	private CodeEnum(String code) {
		this.code = code;
	}
	
	@Override
	public String getPrefix() {
		return "RDS";
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

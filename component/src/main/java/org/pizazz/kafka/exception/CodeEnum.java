package org.pizazz.kafka.exception;

import org.pizazz.message.ref.IMessageCode;

public enum CodeEnum implements IMessageCode {
	
	KAF_0000("KAF0000#"),
	/** 不支持的订阅模式 */
	KAF_0001("KAF0001#");

	private String code;
	private StringBuffer message;
	private final Object lock = new Object();

	private CodeEnum(String code) {
		this.code = code;
	}
	
	@Override
	public String getPrefix() {
		return "KAF";
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

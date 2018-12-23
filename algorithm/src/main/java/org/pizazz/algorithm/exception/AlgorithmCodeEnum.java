package org.pizazz.algorithm.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 算法识别码
 * 
 * @author xlgp2171
 * @version 1.0.181223
 */
public enum AlgorithmCodeEnum implements IMessageCode {
	/** 参数异常 */
	ALG_0001("ALG0001#"),
	/** 参数控制 */
	ALG_0002("ALG0002#");

	private String code;
	private StringBuffer message;
	private final Object lock = new Object();

	private AlgorithmCodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getPrefix() {
		return "ALG";
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

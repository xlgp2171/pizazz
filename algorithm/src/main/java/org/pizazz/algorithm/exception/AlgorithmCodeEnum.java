package org.pizazz.algorithm.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 算法识别码
 * 
 * @author xlgp2171
 * @version 1.1.190122
 */
public enum AlgorithmCodeEnum implements IMessageCode {
	/** 参数异常 */
	ALG_0001("ALG0001#"),
	/** 参数控制 */
	ALG_0002("ALG0002#");

	private String code;

	private AlgorithmCodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getPrefix() {
		return "ALG";
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public StringBuffer append(Object target) {
		return new StringBuffer(code).append(target);
	}
}

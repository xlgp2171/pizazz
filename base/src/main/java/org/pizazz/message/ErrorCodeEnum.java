package org.pizazz.message;

import org.pizazz.message.ref.IMessageCode;

/**
 * 内核错误识别码
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public enum ErrorCodeEnum implements IMessageCode {
	/** 未知的异常 */
	ERR_0000("ERR0000#"),
	/** 未知操作系统 */
	ERR_0001("ERR0001#"),
	/** 异常包装 */
	ERR_0002("ERR0002#"),
	/** 必要对外连接开启错误 */
	ERR_0003("ERR0003#"),
	/** 必要对外连接接收数据错误 */
	ERR_0004("ERR0004#");

	private String code;
	private StringBuffer message;

	private ErrorCodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getPrefix() {
		return "ERR";
	}

	@Override
	public String getValue() {
		return message == null ? code : code + message.toString();
	}

	@Override
	public IMessageCode append(Object target) {
		if (message == null) {
			synchronized (message) {
				if (message == null) {
					message = new StringBuffer();
				}
			}
		}
		message.append(target);
		return this;
	}
}

package org.pizazz.exception;

import org.pizazz.common.StringUtils;
import org.pizazz.message.IMessageCode;

/**
 * 内核运行异常<br>
 * 内核运行异常全部对应消息识别码
 * 
 * @author xlgp2171
 * @version 1.0.181210
 * 
 * @see IMessageCode
 */
public class BaseError extends RuntimeException {
	private static final long serialVersionUID = -3508124189871042223L;

	public BaseError(IMessageCode code) {
		this(code, StringUtils.EMPTY);
	}

	public BaseError(IMessageCode code, String message) {
		super(code.append(message).getValue());
	}

	public BaseError(IMessageCode code, Throwable cause) {
		this(code, StringUtils.EMPTY, cause);
	}

	public BaseError(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).getValue(), cause);
	}
}

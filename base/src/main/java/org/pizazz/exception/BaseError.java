package org.pizazz.exception;

import org.pizazz.common.StringUtils;
import org.pizazz.message.ref.IMessageCode;

/**
 * 内核运行异常<br>
 * 内核运行异常全部对应消息识别码
 * 
 * @author xlgp2171
 * @version 1.0.181210
 * 
 * @see IMessageCode
 */
public class BaseError extends AbstractError {
	private static final long serialVersionUID = -3508124189871042223L;

	public BaseError(IMessageCode code) {
		this(code, StringUtils.EMPTY);
	}

	public BaseError(IMessageCode code, String message) {
		super(code, message);
	}

	public BaseError(IMessageCode code, Throwable cause) {
		super(code, cause);
	}

	public BaseError(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

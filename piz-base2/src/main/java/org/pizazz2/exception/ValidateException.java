package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 断言验证运行时异常
 * 
 * @author xlgp2171
 * @version 2.1.211103
 * 
 * @see IMessageCode
 */
public class ValidateException extends BaseRuntimeException {
	private static final long serialVersionUID = 7710998580948211094L;

	public ValidateException(IMessageCode code, String message) {
		super(code, message);
	}

	public ValidateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidateException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

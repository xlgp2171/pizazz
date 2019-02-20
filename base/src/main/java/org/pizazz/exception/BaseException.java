package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 基础异常
 * 
 * @author xlgp2171
 * @version 1.1.190220
 * 
 * @see IMessageCode
 */
public class BaseException extends AbstractException {
	private static final long serialVersionUID = 6106125903101600702L;

	public BaseException(IMessageCode code, String message) {
		super(code, message);
	}

	public BaseException(IMessageCode code, Throwable cause) {
		super(code, cause);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

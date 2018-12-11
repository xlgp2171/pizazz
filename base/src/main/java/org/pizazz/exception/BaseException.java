package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 基础异常
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class BaseException extends Exception {
	private static final long serialVersionUID = 6106125903101600702L;

	public BaseException(String message) {
		super(message);
	}

	public BaseException(IMessageCode code, String message) {
		super(code.append(message).getValue());
	}

	public BaseException(Throwable cause) {
		super(cause);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).getValue(), cause);
	}
}

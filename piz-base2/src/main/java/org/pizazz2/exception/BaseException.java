package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 基础异常
 * 
 * @author xlgp2171
 * @version 2.0.210201
 * 
 * @see IMessageCode
 */
public class BaseException extends AbstractException {
	private static final long serialVersionUID = 9062719073208332115L;

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

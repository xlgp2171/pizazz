package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 异常超类
 * 
 * @author xlgp2171
 * @version 1.0.190122
 * 
 * @see IMessageCode
 */
public abstract class AbstractException extends Exception {
	private static final long serialVersionUID = 7305704685296507485L;

	public AbstractException(IMessageCode code, String message) {
		super(code.append(message).toString());
	}

	public AbstractException(IMessageCode code, Throwable cause) {
		this(code.getCode(), cause);
	}

	public AbstractException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbstractException(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).toString(), cause);
	}
}

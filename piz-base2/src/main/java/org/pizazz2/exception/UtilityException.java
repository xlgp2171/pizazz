package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 通用工具使用异常
 * 
 * @author xlgp2171
 * @version 2.1.210917
 * 
 * @see IMessageCode
 */
public class UtilityException extends BaseException {
	private static final long serialVersionUID = 7798259423596221431L;

	public UtilityException(String message, Throwable cause) {
		super(message, cause);
	}

	public UtilityException(IMessageCode code, String message) {
		super(code, message);
	}

	public UtilityException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

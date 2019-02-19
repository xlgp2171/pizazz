package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 通用工具使用异常
 * 
 * @author xlgp2171
 * @version 1.0.190219
 * 
 * @see IMessageCode
 */
public class UtilityException extends BaseException {
	private static final long serialVersionUID = 1L;

	public UtilityException(IMessageCode code, String message) {
		super(code, message);
	}

	public UtilityException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

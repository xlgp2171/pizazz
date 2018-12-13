package org.pizazz.log.exception;

import org.pizazz.exception.BaseError;
import org.pizazz.message.ref.IMessageCode;

/**
 * 日志异常
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class LogError extends BaseError {
	private static final long serialVersionUID = -8862701867465595519L;

	public LogError(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

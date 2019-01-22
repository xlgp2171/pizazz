package org.pizazz.exception;

import org.pizazz.common.StringUtils;
import org.pizazz.message.ref.IMessageCode;

/**
 * 错误超类
 * 
 * @author xlgp2171
 * @version 1.0.190122
 * 
 * @see IMessageCode
 */
public class AbstractError extends RuntimeException {
	private static final long serialVersionUID = 3378032776400875141L;

	public AbstractError(IMessageCode code, String message) {
		super(code.append(message).toString());
	}

	public AbstractError(IMessageCode code, Throwable cause) {
		this(code, StringUtils.EMPTY, cause);
	}

	public AbstractError(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).toString(), cause);
	}
}

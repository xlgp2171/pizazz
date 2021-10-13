package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 非法操作运行时异常<br>
 * 包括非法参数、非法状态、非法格式、非法使用等
 * 
 * @author xlgp2171
 * @version 2.1.210917
 * 
 * @see IMessageCode
 */
public class IllegalException extends ValidateException {

	private static final long serialVersionUID = 5719654786413369117L;

	public IllegalException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalException(IMessageCode code, String message) {
		super(code, message);
	}

	public IllegalException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

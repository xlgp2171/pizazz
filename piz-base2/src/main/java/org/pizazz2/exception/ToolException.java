package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 实用工具使用异常
 * 
 * @author xlgp2171
 * @version 1.0.190220
 * 
 * @see IMessageCode
 */
public class ToolException extends BaseException {
	private static final long serialVersionUID = 6227140192148956885L;

	public ToolException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ToolException(IMessageCode code, String message) {
		super(code, message);
	}

	public ToolException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}
}

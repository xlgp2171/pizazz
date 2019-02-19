package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

/**
 * 断言验证异常
 * 
 * @author xlgp2171
 * @version 1.0.190202
 * 
 * @see IMessageCode
 */
public class AssertException extends BaseException {
	private static final long serialVersionUID = 1L;

	public AssertException(IMessageCode code, String message) {
		super(code, message);
	}
}

package org.pizazz.exception;

import org.pizazz.message.ref.IMessageCode;

public class AssertException extends BaseException {
	private static final long serialVersionUID = 1L;

	public AssertException(IMessageCode code, String message) {
		super(code, message);
	}
}

package org.pizazz.berkleydb.exception;

import org.pizazz.exception.AbstractException;
import org.pizazz.message.ref.IMessageCode;

public class BDBException extends AbstractException {
	private static final long serialVersionUID = 1196954810505548106L;

	public BDBException(IMessageCode code, String message) {
		super(code, message);
	}

	public BDBException(IMessageCode code, Throwable cause) {
		super(code, cause);
	}

	public BDBException(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).toString(), cause);
	}
}

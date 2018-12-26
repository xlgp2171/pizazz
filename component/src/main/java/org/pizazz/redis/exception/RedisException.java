package org.pizazz.redis.exception;

import org.pizazz.exception.AbstractException;
import org.pizazz.message.ref.IMessageCode;

public class RedisException extends AbstractException {
	private static final long serialVersionUID = -814615372025508720L;

	public RedisException(IMessageCode code, String message) {
		super(code, message);
	}

	public RedisException(IMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}

}

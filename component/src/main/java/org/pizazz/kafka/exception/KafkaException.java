package org.pizazz.kafka.exception;

import org.pizazz.exception.AbstractException;
import org.pizazz.message.ref.IMessageCode;

public class KafkaException extends AbstractException {
	private static final long serialVersionUID = 5169096434028278101L;

	public KafkaException(IMessageCode code, String message) {
		super(code, message);
	}

	public KafkaException(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).toString(), cause);
	}
}

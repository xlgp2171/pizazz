package org.pizazz.kafka.exception;

import org.pizazz.exception.AbstractError;
import org.pizazz.message.ref.IMessageCode;

public class KafkaError extends AbstractError {
	private static final long serialVersionUID = 6718273375480546118L;

	public KafkaError(IMessageCode code, String message) {
		super(code, message);
	}
}

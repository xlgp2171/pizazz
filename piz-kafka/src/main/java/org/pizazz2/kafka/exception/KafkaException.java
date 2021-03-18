package org.pizazz2.kafka.exception;

import org.pizazz2.exception.BaseException;
import org.pizazz2.message.ref.IMessageCode;

/**
 * kafka 异常类
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class KafkaException extends BaseException {
	private static final long serialVersionUID = 2844318072800219652L;

	public KafkaException(IMessageCode code, String message) {
		super(code, message);
	}

	public KafkaException(IMessageCode code, String message, Throwable cause) {
		super(code.append(message).toString(), cause);
	}
}

package org.pizazz.algorithm.exception;

import org.pizazz.exception.AbstractException;
import org.pizazz.message.ref.IMessageCode;

/**
 * 算法异常
 * 
 * @author xlgp2171
 * @version 1.0.181223
 */
public class AlgorithmException extends AbstractException {
	private static final long serialVersionUID = -4636869181270961593L;

	public AlgorithmException(IMessageCode code, String message) {
		super(code, message);
	}
}

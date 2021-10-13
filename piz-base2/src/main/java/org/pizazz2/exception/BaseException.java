package org.pizazz2.exception;

import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 基础异常
 * 
 * @author xlgp2171
 * @version 2.1.210917
 * 
 * @see IMessageCode
 */
public class BaseException extends Exception implements IException {

	private static final long serialVersionUID = -3906001664314803110L;
	private final IMessageCode code;

	public BaseException(IMessageCode code, String message) {
		super(message);
		this.code = code;
	}

	public BaseException(IMessageCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
		this.code = BasicCodeEnum.MSG_0000;
	}

	public BaseException(IMessageCode code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	@Override
	public IMessageCode getMessageCode() {
		return code;
	}

	@Override
	public String getNativeMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		return code == null || code == BasicCodeEnum.MSG_0000 ?
				getNativeMessage() : code.append(getNativeMessage()).toString();
	}
}

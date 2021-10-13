package org.pizazz2.exception;

import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 断言验证运行时异常
 * 
 * @author xlgp2171
 * @version 2.1.210917
 * 
 * @see IMessageCode
 */
public class ValidateException extends RuntimeException implements IException {

	private static final long serialVersionUID = 7710998580948211094L;
	private final IMessageCode code;

	public ValidateException(IMessageCode code, String message) {
		super(message);
		this.code = code;
	}

	public ValidateException(IMessageCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public ValidateException(String message, Throwable cause) {
		super(message, cause);
		this.code = BasicCodeEnum.MSG_0000;
	}

	public ValidateException(IMessageCode code, String message, Throwable cause) {
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

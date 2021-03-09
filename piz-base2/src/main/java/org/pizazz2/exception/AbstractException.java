package org.pizazz2.exception;

import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 异常基类
 *
 * @author xlgp2171
 * @version 2.0.210201
 * @see IMessageCode
 */
public abstract class AbstractException extends Exception implements IException {
    private static final long serialVersionUID = 6179533545008774989L;
    private final IMessageCode code;

    public AbstractException(IMessageCode code, String message) {
        super(message);
        this.code = code;
    }

    public AbstractException(IMessageCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public AbstractException(String message, Throwable cause) {
        super(message, cause);
        this.code = BasicCodeEnum.MSG_0000;
    }

    public AbstractException(IMessageCode code, String message, Throwable cause) {
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
        return code == null ? getNativeMessage() : code.append(getNativeMessage()).toString();
    }
}

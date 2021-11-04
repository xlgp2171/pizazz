package org.pizazz2.exception;

import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 基础运行时异常
 *
 * @author xlgp2171
 * @version 2.0.211103
 *
 * @see IMessageCode
 */
public class BaseRuntimeException extends RuntimeException implements IException {
    private static final long serialVersionUID = -503681173090440605L;
    private final IMessageCode code;

    public BaseRuntimeException(IMessageCode code, String message) {
        super(message);
        this.code = code;
    }

    public BaseRuntimeException(IMessageCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public BaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.code = BasicCodeEnum.MSG_0000;
    }

    public BaseRuntimeException(IMessageCode code, String message, Throwable cause) {
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

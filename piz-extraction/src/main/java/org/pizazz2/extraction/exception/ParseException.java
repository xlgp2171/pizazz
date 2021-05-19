package org.pizazz2.extraction.exception;

import org.pizazz2.exception.BaseException;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 文本解析异常
 *
 * @author xlgp2171
 * @version 2.0.210401
 */
public class ParseException extends BaseException {
    private static final long serialVersionUID = -6016897346399886519L;

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(IMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public ParseException(IMessageCode code, String message) {
        super(code, message);
    }
}

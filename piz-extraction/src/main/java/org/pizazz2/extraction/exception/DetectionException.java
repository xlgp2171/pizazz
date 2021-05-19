package org.pizazz2.extraction.exception;

import org.pizazz2.exception.BaseException;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 文本识别异常
 *
 * @author xlgp2171
 * @version 2.0.210401
 */
public class DetectionException extends BaseException {
    private static final long serialVersionUID = 3992073412847211124L;

    public DetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DetectionException(IMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public DetectionException(IMessageCode code, String message) {
        super(code, message);
    }
}

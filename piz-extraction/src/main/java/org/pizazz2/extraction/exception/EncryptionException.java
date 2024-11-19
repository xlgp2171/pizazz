package org.pizazz2.extraction.exception;

import org.pizazz2.exception.BaseException;
import org.pizazz2.message.ref.IMessageCode;

/**
 * 无法解密异常
 *
 * @author xlgp2171
 * @version 2.2.240627
 */
public class EncryptionException extends BaseException {
    private static final long serialVersionUID = -3850071023587805785L;

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(IMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public EncryptionException(IMessageCode code, String message) {
        super(code, message);
    }
}

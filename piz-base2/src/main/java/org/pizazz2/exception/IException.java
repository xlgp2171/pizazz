package org.pizazz2.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 异常接口
 *
 * @author xlgp2171
 * @version 2.0.210201
 * @see IMessageCode
 */
public interface IException {
    /**
     * 返回消息码
     *
     * @return 消息码
     */
    IMessageCode getMessageCode();

    /**
     * 获取原生的Message
     *
     * @return 原生Message
     */
    String getNativeMessage();

    /**
     * 获取异常输出的Message
     *
     * @return 输出Message
     */
    String getMessage();
}

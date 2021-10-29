package org.pizazz2.kafka.producer;

import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.exception.CodeEnum;

/**
 * 发布模式枚举
 *
 * @author xlgp2171
 * @version 2.0.211015
 */
public enum ProducerModeEnum {
    /**
     * 异步事务发送
     */
    ASYNC_TRANSACTION(false, true),
    /**
     * 同步事务发送
     */
    SYNC_TRANSACTION(true, true),
    /**
     * 异步发送
     */
    ASYNC(false, false),
    /**
     * 同步发送
     */
    SYNC(true, false);

    private final boolean isSync;
    private final boolean isTransaction;

    ProducerModeEnum(boolean isSync, boolean isTransaction) {
        this.isSync = isSync;
        this.isTransaction = isTransaction;
    }

    public boolean isSync() {
        return isSync;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public static ProducerModeEnum from(String mode) throws ValidateException {
        ValidateUtils.notNull("from", mode);
        mode = mode.trim().toUpperCase();

        for (ProducerModeEnum item : values()) {
            if (item.name().equals(mode)) {
                return item;
            }
        }
        throw new IllegalException(CodeEnum.KFK_0011, mode);
    }
}

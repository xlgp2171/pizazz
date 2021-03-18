package org.pizazz2.kafka.consumer;

import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;

/**
 * 接收模式枚举
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public enum ConsumerModeEnum {
    /**
     * 自动异步一轮
     */
    AUTO_ASYNC_ROUND(true, false, false),
    /**
     * 手动异步每个
     */
    MANUAL_ASYNC_EACH(false, false, true),
    /**
     * 手动同步每个
     */
    MANUAL_SYNC_EACH(false, true, true),
    /**
     * 手动异步一轮
     */
    MANUAL_ASYNC_ROUND(false, false, false),
    /**
     * 手动同步一轮
     */
    MANUAL_SYNC_ROUND(false, true, false),
    /**
     * 手动无提交
     */
    MANUAL_NONE_NONE(false, false, false);

    private final boolean isAuto;
    private final boolean isSync;
    private final boolean isEach;

    ConsumerModeEnum(boolean isAuto, boolean isSync, boolean isEach) {
        this.isAuto = isAuto;
        this.isSync = isSync;
        this.isEach = isEach;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public final boolean isSync() {
        return isSync;
    }

    public final boolean isEach() {
        return isEach;
    }

    public static ConsumerModeEnum from(String mode) throws ValidateException, KafkaException {
        ValidateUtils.notNull("from", mode);
        mode = mode.trim().toUpperCase();

        for (ConsumerModeEnum item : values()) {
            if (item.name().equals(mode)) {
                return item;
            }
        }
        throw new KafkaException(CodeEnum.KFK_0007, mode);
    }
}

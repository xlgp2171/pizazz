package org.pizazz2.kafka.consumer;

import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.kafka.exception.CodeEnum;

/**
 * 消费忽略异常枚举
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public enum ConsumerIgnoreEnum {
    /**
     * 忽略offset和consume异常
     */
    OFFSET_CONSUME(false, false),
    /**
     * 忽略consume异常
     */
    CONSUME(true, false),
    /**
     * 忽略offset异常
     */
    OFFSET(false, true),
    /**
     * 无任何忽略
     */
    NONE(true, true);

    private final boolean offset;
    private final boolean consume;

    ConsumerIgnoreEnum(boolean offset, boolean consume) {
        this.offset = offset;
        this.consume = consume;
    }

    public boolean offsetThrowable() {
        return offset;
    }

    public boolean consumeThrowable() {
        return consume;
    }

    public static ConsumerIgnoreEnum from(String mode) throws IllegalException {
        ValidateUtils.notNull("from", mode);
        mode = mode.trim().toUpperCase();

        for (ConsumerIgnoreEnum item : values()) {
            if (item.name().equals(mode)) {
                return item;
            }
        }
        throw new IllegalException(CodeEnum.KFK_0008, mode);
    }
}

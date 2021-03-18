package org.pizazz2.kafka.consumer;

import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;

/**
 * 消费忽略异常枚举
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public enum ConsumerIgnoreEnum {
    /**
     * 忽略offset和consume异常
     */
    OFFSET_CONSUME(false, false),
    /**
     * 忽略consume异常
     */
    CONSUME(false, true),
    /**
     * 忽略offset异常
     */
    OFFSET(true, false),
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

    public static ConsumerIgnoreEnum from(String mode) throws ValidateException, KafkaException {
        ValidateUtils.notNull("from", mode);
        mode = mode.trim().toUpperCase();

        for (ConsumerIgnoreEnum item : values()) {
            if (item.name().equals(mode)) {
                return item;
            }
        }
        throw new KafkaException(CodeEnum.KFK_0008, mode);
    }
}

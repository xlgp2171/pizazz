package org.pizazz.kafka.consumer;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;

public enum ConsumerIgnoreEnum {
	/** 忽略offset和consume异常 */
	OFFSET_CONSUME(false, false),
	/** 忽略consume异常 */
	CONSUME(false, true),
	/** 忽略offset异常 */
	OFFSET(true, false),
	/** 无任何忽略 */
	NONE(true, true);
	
	private final boolean offset;
	private final boolean consume;

	private ConsumerIgnoreEnum(boolean offset, boolean consume) {
		this.offset = offset;
		this.consume = consume;
	}

	public boolean offsetThrowable() {
		return offset;
	}

	public boolean consumeThrowable() {
		return consume;
	}

	public static ConsumerIgnoreEnum from(String mode) throws AssertException, KafkaException {
		AssertUtils.assertNotNull("from", mode);
		mode = mode.trim().toUpperCase();

		for (ConsumerIgnoreEnum _item : values()) {
			if (_item.name().equals(mode)) {
				return _item;
			}
		}
		throw new KafkaException(CodeEnum.KFK_0008, mode);
	}
}

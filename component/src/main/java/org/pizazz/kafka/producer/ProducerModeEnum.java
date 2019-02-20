package org.pizazz.kafka.producer;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;

public enum ProducerModeEnum {
	/** 异步事务发送 */
	ASYNC_TRANSACTION(false, true),
	/** 同步事务发送 */
	SYNC_TRANSACTION(false, true),
	/** 异步发送 */
	ASYNC(true, false),
	/** 同步发送 */
	SYNC(true, false);

	private final boolean isSync;
	private final boolean isTransaction;

	private ProducerModeEnum(boolean isSync, boolean isTransaction) {
		this.isSync = isSync;
		this.isTransaction =isTransaction;
	}

	public boolean isSync() {
		return isSync;
	}

	public boolean isTransaction() {
		return isTransaction;
	}

	public static ProducerModeEnum from(String mode) throws AssertException, KafkaException {
		AssertUtils.assertNotNull("from", mode);
		mode = mode.trim().toUpperCase();

		for (ProducerModeEnum _item : values()) {
			if (_item.name().equals(mode)) {
				return _item;
			}
		}
		throw new KafkaException(CodeEnum.KFK_0011, mode);
	}
}

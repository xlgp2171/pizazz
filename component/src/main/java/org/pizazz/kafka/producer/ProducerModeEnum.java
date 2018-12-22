package org.pizazz.kafka.producer;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;

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

	public static ProducerModeEnum from(String mode) throws BaseException {
		AssertUtils.assertNotNull("from", mode);
		mode = mode.trim().toUpperCase();

		for (ProducerModeEnum _item : values()) {
			if (_item.name().equals(mode)) {
				return _item;
			}
		}
		throw new BaseException(CodeEnum.KFK_0011, mode);
	}
}

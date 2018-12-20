package org.pizazz.kafka.consumer;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;

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
	MANUAL_SYNC_ROUND(false, true, false);

	private final boolean isAuto;
	private final boolean isSync;
	private final boolean isEach;

	private ConsumerModeEnum(boolean isAuto, boolean isSync, boolean isEach) {
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

	public static ConsumerModeEnum from(String mode) throws BaseException {
		AssertUtils.assertNotNull("from", mode);
		mode = mode.trim().toUpperCase();

		for (ConsumerModeEnum _item : values()) {
			if (_item.name().equals(mode)) {
				return _item;
			}
		}
		throw new BaseException(CodeEnum.KFK_0007, mode);
	}
}

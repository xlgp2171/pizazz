package org.pizazz.kafka.ref;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.tool.AbstractClassPlugin;

public abstract class AbstractClient extends AbstractClassPlugin {

	private final AtomicBoolean initialized = new AtomicBoolean(false);

	private ConfigConvertor convertor;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		if (!initialized.compareAndSet(false, true)) {
			throw new BaseException(BasicCodeEnum.MSG_0020, "subscription initialized");
		}
		// 创建配置类
		convertor = new ConfigConvertor(config, _item -> _item);
	}

	protected boolean isInitialize() {
		return initialized.get();
	}

	protected ConfigConvertor getConvertor() {
		return convertor;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (isInitialize()) {
			SystemUtils.destroy(convertor, timeout);
		}
	}
}

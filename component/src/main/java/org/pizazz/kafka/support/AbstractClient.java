package org.pizazz.kafka.support;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClient extends AbstractClassPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);
	private final AtomicBoolean initialized = new AtomicBoolean(false);

	private ConfigConvertor convertor;

	@Override
	public void initialize(TupleObject config) throws KafkaException, AssertException, UtilityException, ToolException {
		if (!initialized.compareAndSet(false, true)) {
			throw new KafkaException(BasicCodeEnum.MSG_0020, "client initialized");
		}
		// 创建配置类
		convertor = new ConfigConvertor(config);
	}

	protected boolean isInitialize() {
		return initialized.get();
	}

	protected ConfigConvertor getConvertor() {
		return convertor;
	}

	@Override
	protected void log(String msg, BaseException e) {
		if (e != null) {
			LOGGER.error(msg, e);
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);
		}
	}

	@Override
	public void destroy(Duration timeout) {
		if (isInitialize()) {
			SystemUtils.destroy(convertor, timeout);
		}
	}
}

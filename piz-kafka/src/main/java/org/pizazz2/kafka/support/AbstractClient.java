package org.pizazz2.kafka.support;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz2.ICloseable;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.*;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端组件基类
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public abstract class AbstractClient extends AbstractClassPlugin<TupleObject> implements ICloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

	private ConfigConvertor convertor;

	public AbstractClient(TupleObject configure) throws ValidateException, BaseException {
		super(configure);
		setUpConfig();
	}

	protected void setUpConfig() throws ValidateException, BaseException {
		// 创建配置类
		convertor = new ConfigConvertor(super.getConfig());
	}

	protected ConfigConvertor getConvertor() {
		return convertor;
	}

	@Override
	protected void log(String message, BaseException exception) {
		if (exception != null) {
			LOGGER.error(message, exception);
		} else {
			LOGGER.debug(message);
		}
	}

	@Override
	public void destroy(Duration timeout) {
		SystemUtils.destroy(convertor, timeout);
	}
}

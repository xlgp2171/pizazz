package org.pizazz2.kafka.core;

import java.time.Duration;

import org.pizazz2.ICloseable;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.*;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端组件基类
 *
 * @author xlgp2171
 * @version 2.1.220625
 */
public abstract class AbstractClient extends AbstractClassPlugin<TupleObject> implements ICloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

	private final ConfigConvertor convertor;

	public AbstractClient(TupleObject configure) throws BaseRuntimeException, BaseException {
		super(configure);
		// 创建配置类
		convertor = new ConfigConvertor(super.getConfig());
		// 立即初始化
		initialize();
	}

	/**
	 * 初始化方法
	 * @throws BaseRuntimeException 初始化运行时异常
	 * @throws BaseException 初始化异常
	 */
	protected abstract void initialize() throws BaseRuntimeException, BaseException;

	protected ConfigConvertor getConvertor() {
		return convertor;
	}

	@Override
	protected void log(String message, BaseException exception) {
		if (exception != null) {
			LOGGER.error(message, exception);
		} else if (KafkaConstant.DEBUG_MODE) {
			LOGGER.debug(message);
		}
	}

	@Override
	public void destroy(Duration timeout) {
		SystemUtils.destroy(convertor, timeout);
	}
}

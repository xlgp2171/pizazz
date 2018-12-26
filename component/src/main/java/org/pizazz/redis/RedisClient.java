package org.pizazz.redis;

import java.time.Duration;

import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.redis.redisson.RedissonAdapter;
import org.pizazz.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisClient extends AbstractClassPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);
	private IRedisInstance instance;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		updateConfig(config);
		instance = cast(loadPlugin("classpath", new RedissonAdapter(), null, true), IRedisInstance.class);
		LOGGER.info("redis initialized,config=" + config);
	}

	public IRedisProcessor getProcessor() {
		return instance.getProcessor();
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
	public void destroy(Duration timeout) throws BaseException {
		SystemUtils.destroy(instance, timeout);
		LOGGER.info("redis destroyed,timeout=" + timeout);
	}
}

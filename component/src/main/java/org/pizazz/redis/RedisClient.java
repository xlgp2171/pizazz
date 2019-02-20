package org.pizazz.redis;

import java.time.Duration;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.redis.exception.RedisException;
import org.pizazz.redis.redisson.RedissonAdapter;
import org.pizazz.tool.AbstractClassPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisClient extends AbstractClassPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);
	private IRedisAdapter adapter;

	@Override
	public void initialize(TupleObject config) throws RedisException, AssertException, UtilityException, ToolException {
		updateConfig(config);
		adapter = cast(loadPlugin("classpath", new RedissonAdapter(), null, true), IRedisAdapter.class);
		LOGGER.info("redis initialized,config=" + config);
	}

	public IRedisProcessor getProcessor() {
		return adapter.getProcessor();
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
		unloadPlugin(adapter, timeout);
		LOGGER.info("redis destroyed,timeout=" + timeout);
	}
}

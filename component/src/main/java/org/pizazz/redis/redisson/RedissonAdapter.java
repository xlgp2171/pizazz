package org.pizazz.redis.redisson;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.pizazz.common.JSONUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.UtilityException;
import org.pizazz.redis.IRedisAdapter;
import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.RedisConstant;
import org.pizazz.redis.exception.CodeEnum;
import org.pizazz.redis.exception.RedisException;
import org.redisson.config.Config;

public class RedissonAdapter implements IRedisAdapter {
	private RedissonInstance instance;

	@Override
	public void initialize(TupleObject config) throws RedisException, UtilityException {
		TupleObject _config = TupleObjectHelper.getTupleObject(config, RedisConstant.KEY_CLIENT);
		String _tmp = JSONUtils.toJSON(_config);
		try {
			instance = new RedissonInstance(Config.fromJSON(_tmp));
		} catch (IOException e) {
			throw new RedisException(CodeEnum.RDS_0001, "config:" + _config, e);
		}
	}

	@Override
	public IRedisProcessor getProcessor() {
		return new RedissonProcessor(instance);
	}

	@Override
	public void destroy(Duration timeout) {
		if (timeout == null || timeout.isNegative() || timeout.isZero()) {
			instance.shutdown();
		} else {
			instance.shutdown(timeout.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS);
		}
	}
}

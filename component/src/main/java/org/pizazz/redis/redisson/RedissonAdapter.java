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
		TupleObject _clientC = TupleObjectHelper.getTupleObject(config, RedisConstant.KEY_CLIENT);
		TupleObject _configC = TupleObjectHelper.getTupleObject(config, RedisConstant.KEY_CONFIG);
		String _tmp = JSONUtils.toJSON(_clientC);
		Config _conf = null;
		try {
			_conf = Config.fromJSON(_tmp);
		} catch (IOException e) {
			throw new RedisException(CodeEnum.RDS_0001, "config:" + config, e);
		}
		if (!TupleObjectHelper.getBoolean(_configC, "defCodec", false)) {
			// reddisson默认使用jackson编码方式，修改为强制兼容其它
			_conf.setCodec(new org.redisson.client.codec.StringCodec());
		}
		instance = new RedissonInstance(_conf);
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

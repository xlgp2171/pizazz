package org.pizazz.redis.jedis;

import java.net.URI;
import java.time.Duration;

import org.pizazz.common.ClassUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.PathUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.redis.IRedisAdapter;
import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.RedisConstant;
import org.pizazz.redis.exception.RedisException;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisAdapter implements IRedisAdapter {
	private JedisPool pool;

	@Override
	public void initialize(TupleObject config) throws RedisException, AssertException, UtilityException {
		TupleObject _config = TupleObjectHelper.getTupleObject(config, RedisConstant.KEY_CLIENT);
		JedisPoolConfig _poolConfig = new JedisPoolConfig();
		ClassUtils.simplePopulate(_poolConfig, _config);
		// URI: redis://user:pwd@192.168.0.1:6379
		URI _uri = PathUtils.toURI(TupleObjectHelper.getString(_config, "uri", StringUtils.EMPTY));
		int _timeout = TupleObjectHelper.getInt(_config, "timeout", RedisConstant.DEF_TIMEOUT);
		pool = new JedisPool(_poolConfig, _uri, _timeout);
	}

	@Override
	public IRedisProcessor getProcessor() {
		return new JedisProcessor(pool);
	}

	@Override
	public void destroy(Duration timeout) {
		IOUtils.close(pool);
	}
}

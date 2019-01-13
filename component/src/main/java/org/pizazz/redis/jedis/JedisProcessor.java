package org.pizazz.redis.jedis;

import java.util.Map;
import java.util.function.Function;

import org.pizazz.common.StringUtils;
import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.RedisHelper;
import org.pizazz.redis.exception.CodeEnum;
import org.pizazz.redis.exception.RedisException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisProcessor implements IRedisProcessor {
	private final JedisPool pool;

	public JedisProcessor(JedisPool pool) {
		this.pool = pool;
	}

	protected <R> R tryMethod(String method, Function<Jedis, R> function) throws RedisException {
		try (Jedis _jedis = pool.getResource()) {
			return function.apply(_jedis);
		} catch (Exception e) {
			throw new RedisException(CodeEnum.RDS_0002, method + ":" + e.getMessage(), e);
		}
	}

	@Override
	public String set(String key, String value) throws RedisException {
		return tryMethod("set", _item -> _item.set(key, value));
	}

	@Override
	public String bset(String key, byte[] value) throws RedisException {
		return tryMethod("bset", _item -> _item.set(RedisHelper.fromString(key), value));
	}

	@Override
	public String hmset(String key, Map<String, String> map) throws RedisException {
		return tryMethod("hmset", _item -> _item.hmset(key, map));
	}

	@Override
	public String hset(String key, String field, String value) throws RedisException {
		return StringUtils.of(tryMethod("hset", _item -> _item.hset(key, field, value)));
	}

	@Override
	public String get(String key) throws RedisException {
		return tryMethod("get", _item -> _item.get(key));
	}

	@Override
	public byte[] bget(String key) throws RedisException {
		return tryMethod("bget", _item -> _item.get(RedisHelper.fromString(key)));
	}

	@Override
	public String hget(String key, String field) throws RedisException {
		return tryMethod("hget", _item -> _item.hget(key, field));
	}

	@Override
	public Map<String, String> hmget(String key) throws RedisException {
		return tryMethod("hmget", _item -> _item.hgetAll(key));
	}

	@Override
	public String hdel(String key, String field) throws RedisException {
		return StringUtils.of(tryMethod("bset", _item -> _item.hdel(key, field)));
	}

	@Override
	public boolean del(String key) throws RedisException {
		return tryMethod("bset", _item -> _item.del(key)) > 0;
	}

}

package org.pizazz.redis.redisson;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.RedisConstant;
import org.pizazz.redis.RedisHelper;
import org.pizazz.redis.exception.CodeEnum;
import org.pizazz.redis.exception.RedisException;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RMap;

public class RedissonProcessor implements IRedisProcessor {
	private final RedissonInstance instance;

	RedissonProcessor(RedissonInstance instance) {
		this.instance = instance;
	}

	protected <T> T tryMethod(String method, Supplier<T> supplier) throws RedisException {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new RedisException(CodeEnum.RDS_0002, method + ":" + e.getMessage(), e);
		}
	}

	private String tryMethod(String method, Runnable runnable) throws RedisException {
		return tryMethod(method, () -> {
			runnable.run();
			return RedisConstant.STATUS_OK;
		});
	}

	@Override
	public String set(String key, String value) throws RedisException {
		return bset(key, RedisHelper.fromString(value));
	}

	@Override
	public String set(String key, String value, int timeToLive) throws RedisException {
		return bset(key, RedisHelper.fromString(value), timeToLive);
	}

	@Override
	public String bset(String key, byte[] value) throws RedisException {
		return tryMethod("bset", () -> instance.getBinaryStream(key).set(value));
	}

	@Override
	public String bset(String key, byte[] value, int timeToLive) throws RedisException {
		return tryMethod("bsetT", () -> {
			RBinaryStream _tmp = instance.getBinaryStream(key);
			_tmp.set(value);
			_tmp.expire(timeToLive, TimeUnit.SECONDS);
		});
	}

	@Override
	public String hmset(String key, Map<String, String> map) throws RedisException {
		return tryMethod("hmset", () -> instance.getSSMap(key).putAll(map));
	}

	@Override
	public String hmset(String key, Map<String, String> map, int timeToLive) throws RedisException {
		return tryMethod("hmsetT", () -> {
			RMap<String, String> _tmp = instance.getSSMap(key);
			_tmp.putAll(map);
			_tmp.expire(timeToLive, TimeUnit.SECONDS);
		});
	}

	@Override
	public String hset(String key, String field, String value) throws RedisException {
		return tryMethod("hset", () -> instance.getSSMap(key).put(field, value));
	}

	@Override
	public String hset(String key, String field, String value, int timeToLive) throws RedisException {
		return tryMethod("hsetT", () -> {
			RMap<String, String> _tmp = instance.getSSMap(key);
			_tmp.put(field, value);
			_tmp.expire(timeToLive, TimeUnit.SECONDS);
		});
	}

	@Override
	public String get(String key) throws RedisException {
		return RedisHelper.toString(bget(key));
	}

	@Override
	public byte[] bget(String key) throws RedisException {
		return tryMethod("bget", () -> instance.getBinaryStream(key).get());
	}

	@Override
	public String hget(String key, String field) throws RedisException {
		return tryMethod("hget", () -> instance.getSSMap(key).get(field));
	}

	@Override
	public Map<String, String> hmget(String key) throws RedisException {
		return tryMethod("hmget", () -> instance.getSSMap(key).readAllMap());
	}

	@Override
	public String hdel(String key, String field) throws RedisException {
		return tryMethod("hdel", () -> instance.getSSMap(key).remove(field));
	}

	@Override
	public boolean del(String key) throws RedisException {
		return tryMethod("del", () -> instance.getBinaryStream(key).delete());
	}

	@Override
	public Iterable<String> keys(String pattern) throws RedisException {
		return tryMethod("keys", () -> instance.getKeys().getKeysByPattern(pattern));
	}

	@Override
	public boolean clearExpire(String key) throws RedisException {
		return tryMethod("clearExpire", () -> instance.getBinaryStream(key).clearExpire());
	}
}

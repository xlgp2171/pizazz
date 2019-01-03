package org.pizazz.redis.redisson;

import java.util.Map;
import java.util.function.Supplier;

import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.exception.CodeEnum;
import org.pizazz.redis.exception.RedisException;
import org.pizazz.redis.util.RedisHelper;

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

	private void tryMethod(String method, Runnable runnable) throws RedisException {
		tryMethod(method, () -> {
			runnable.run();
			return null;
		});
	}

	@Override
	public void set(String key, String value) throws RedisException {
		bset(key, RedisHelper.fromString(value));
	}

	@Override
	public void bset(String key, byte[] value) throws RedisException {
		tryMethod("bset", () -> instance.getBinaryStream(key).set(value));
	}

	@Override
	public void hmset(String key, Map<String, String> map) throws RedisException {
		tryMethod("hmset", () -> instance.getSSMap(key).putAll(map));
	}

	@Override
	public void hset(String key, String field, String value) throws RedisException {
		tryMethod("hset", () -> instance.getSSMap(key).put(field, value));
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
}

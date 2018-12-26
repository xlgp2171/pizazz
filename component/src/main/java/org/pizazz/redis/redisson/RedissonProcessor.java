package org.pizazz.redis.redisson;

import java.util.Map;

import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.util.RedisHelper;

public class RedissonProcessor implements IRedisProcessor {
	private final RedissonInstance instance;
	
	RedissonProcessor(RedissonInstance instance) {
		this.instance = instance;
	}

	@Override
	public void set(String key, String value) {
		bset(key, RedisHelper.fromString(value));
	}

	@Override
	public void bset(String key, byte[] value) {
		instance.getBinaryStream(key).set(value);
	}

	@Override
	public void hmset(String key, Map<String, String> map) {
		instance.getSSMap(key).putAll(map);
	}

	@Override
	public void hset(String key, String field, String value) {
		instance.getSSMap(key).put(field, value);
	}

	@Override
	public String get(String key) {
		return RedisHelper.toString(bget(key));
	}

	@Override
	public byte[] bget(String key) {
		return instance.getBinaryStream(key).get();
	}

	@Override
	public String hget(String key, String field) {
		return instance.getSSMap(key).get(field);
	}

	@Override
	public Map<String, String> hmget(String key) {
		return instance.getSSMap(key).readAllMap();
	}

	@Override
	public String hdel(String key, String field) {
		return instance.getSSMap(key).remove(field);
	}

	@Override
	public boolean del(String key) {
		return instance.getBinaryStream(key).delete();
	}
}

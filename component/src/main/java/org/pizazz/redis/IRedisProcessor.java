package org.pizazz.redis;

import java.util.Map;

import org.pizazz.redis.exception.RedisException;

public interface IRedisProcessor {
	public void set(String key, String value) throws RedisException;
	public void bset(String key, byte[] value) throws RedisException;
	public void hmset(String key, Map<String, String> map) throws RedisException;
	public void hset(String key, String field, String value) throws RedisException;

	public String get(String key) throws RedisException;
	public byte[] bget(String key) throws RedisException;
	public String hget(String key, String field) throws RedisException;
	public Map<String, String> hmget(String key) throws RedisException;

	public String hdel(String key, String field) throws RedisException;
	public boolean del(String key) throws RedisException;
}

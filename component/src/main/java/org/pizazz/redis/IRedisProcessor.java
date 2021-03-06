package org.pizazz.redis;

import java.util.Map;

import org.pizazz.redis.exception.RedisException;

public interface IRedisProcessor {
	public String set(String key, String value) throws RedisException;
	public String set(String key, String value, int timeToLive) throws RedisException;
	public String bset(String key, byte[] value) throws RedisException;
	public String bset(String key, byte[] value, int timeToLive) throws RedisException;
	public String hmset(String key, Map<String, String> map) throws RedisException;
	public String hmset(String key, Map<String, String> map, int timeToLive) throws RedisException;
	public String hset(String key, String field, String value) throws RedisException;
	public String hset(String key, String field, String value, int timeToLive) throws RedisException;
	public String mset(Map<String, String> ksvs) throws RedisException;

	public String get(String key) throws RedisException;
	public byte[] bget(String key) throws RedisException;
	public String hget(String key, String field) throws RedisException;
	public Map<String, String> hmget(String key) throws RedisException;
	public Map<String, String> mget(String... keys) throws RedisException;

	public String hdel(String key, String field) throws RedisException;
	public boolean del(String key) throws RedisException;
	public long mdel(String... keys) throws RedisException;

	public Iterable<String> keys(String pattern) throws RedisException;
	public boolean clearExpire(String key) throws RedisException;
}

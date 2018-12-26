package org.pizazz.redis;

import java.util.Map;

public interface IRedisProcessor {
	public void set(String key, String value);
	public void bset(String key, byte[] value);
	public void hmset(String key, Map<String, String> map);
	public void hset(String key, String field, String value);

	public String get(String key);
	public byte[] bget(String key);
	public String hget(String key, String field);
	public Map<String, String> hmget(String key);

	public String hdel(String key, String field);
	public boolean del(String key);
}

package org.pizazz.redis.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.pizazz.common.CollectionUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.redis.IRedisProcessor;
import org.pizazz.redis.RedisConstant;
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
	public String set(String key, String value, int timeToLive) throws RedisException {
		return tryMethod("setT", _item -> {
			String _tmp = _item.set(key, value);
			_item.expire(key, timeToLive);
			return _tmp;
		});
	}

	@Override
	public String bset(String key, byte[] value) throws RedisException {
		return tryMethod("bset", _item -> _item.set(RedisHelper.fromString(key), value));
	}

	@Override
	public String bset(String key, byte[] value, int timeToLive) throws RedisException {
		return tryMethod("bsetT", _item -> {
			String _tmp = _item.set(RedisHelper.fromString(key), value);
			_item.expire(key, timeToLive);
			return _tmp;
		});
	}

	@Override
	public String hmset(String key, Map<String, String> map) throws RedisException {
		return tryMethod("hmset", _item -> _item.hmset(key, map));
	}

	@Override
	public String hmset(String key, Map<String, String> map, int timeToLive) throws RedisException {
		return tryMethod("hmsetT", _item -> {
			String _tmp = _item.hmset(key, map);
			_item.expire(key, timeToLive);
			return _tmp;
		});
	}

	@Override
	public String hset(String key, String field, String value) throws RedisException {
		return StringUtils.of(tryMethod("hset", _item -> _item.hset(key, field, value)));
	}

	@Override
	public String hset(String key, String field, String value, int timeToLive) throws RedisException {
		return StringUtils.of(tryMethod("hset", _item -> {
			long _tmp = _item.hset(key, field, value);
			_item.expire(key, timeToLive);
			return _tmp;
		}));
	}

	@Override
	public String mset(Map<String, String> ksvs) throws RedisException {
		if (CollectionUtils.isEmpty(ksvs)) {
			return RedisConstant.STATUS_FAILED;
		}
		return tryMethod("mset", _item1 -> {
			List<String> _ksvs = new ArrayList<>(ksvs.size());

			for (Map.Entry<String, String> _item2 : ksvs.entrySet()) {
				_ksvs.add(_item2.getKey());
				_ksvs.add(_item2.getValue());
			}
			String[] _tmp = _ksvs.toArray(new String[ksvs.size()]);
			return _item1.mset(_tmp);
		});
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
	public Map<String, String> mget(String... keys) throws RedisException {
		List<String> _values = tryMethod("mget", _item -> _item.mget(keys));
		Map<String, String> _tmp = new HashMap<>();

		for (int _i = 0; _i < keys.length; _i++) {
			_tmp.put(keys[_i], _values.get(_i));
		}
		return _tmp;
	}

	@Override
	public String hdel(String key, String field) throws RedisException {
		return StringUtils.of(tryMethod("hdel", _item -> _item.hdel(key, field)));
	}

	@Override
	public boolean del(String key) throws RedisException {
		return tryMethod("del", _item -> _item.del(key)) > 0;
	}

	@Override
	public long mdel(String... keys) throws RedisException {
		return tryMethod("mdel", _item -> _item.del(keys));
	}

	@Override
	public Iterable<String> keys(String pattern) throws RedisException {
		return tryMethod("keys", _item -> _item.keys(pattern));
	}

	@Override
	public boolean clearExpire(String key) throws RedisException {
		return tryMethod("clearExpire", _item -> _item.persist(key)) > 0;
	}
}

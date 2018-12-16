package org.pizazz.common;

import java.util.Map;
import java.util.Properties;

import org.pizazz.common.ref.IKryoConfig;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;

/**
 * 通用对象工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class TupleObjectHelper {

	public static TupleObject emptyObject() {
		return newObject(1);
	}

	public static TupleObject newObject() {
		return new TupleObject();
	}

	public static TupleObject newObject(int size) {
		return new TupleObject(size);
	}

	public static TupleObject newObject(String key, Object value) {
		return newObject().append(key, value);
	}

	public static boolean isEmpty(TupleObject target) {
		return target == null || target.isEmpty();
	}

	public static byte[] serialize(TupleObject target) throws BaseException {
		return SerializationUtils.serialize(target, new IKryoConfig() {
		});
	}

	public static TupleObject deserialize(byte[] target) throws BaseException {
		return SerializationUtils.deserialize(target, TupleObject.class, new IKryoConfig() {
		});
	}

	@SuppressWarnings("unchecked")
	public static TupleObject toObject(Map<String, ?> target) {
		TupleObject _tmp = newObject(target.size());

		for (Map.Entry<String, ?> _item : target.entrySet()) {
			if (_item.getValue() instanceof Map) {
				_tmp.put(_item.getKey(), toObject((Map<String, ?>) _item.getValue()));
			} else {
				_tmp.put(_item.getKey(), _item.getValue());
			}
		}
		return _tmp;
	}

	public static Properties toProperties(Properties left, TupleObject right) {
		Properties _tmp = left == null ? new Properties() : left;

		if (!isEmpty(right)) {
			_tmp.putAll(right);
		}
		return _tmp;
	}

	public static String getString(TupleObject target, String key, String defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return StringUtils.of(target.get(key));
	}

	public static String[] getStringArray(TupleObject target, String key, String regex, String[] defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		Object _tmp = target.get(key);

		if (_tmp == null) {
			return defValue;
		} else if (_tmp instanceof String[]) {
			try {
				return ClassUtils.cast(_tmp, String[].class);
			} catch (BaseException e) {
				return defValue;
			}
		}
		return StringUtils.of(_tmp).split(regex);
	}

	public static int getInt(TupleObject target, String key, int defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toInt(StringUtils.of(target.get(key)), defValue);
	}

	public static long getLong(TupleObject target, String key, long defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toLong(StringUtils.of(target.get(key)), defValue);
	}

	public static short getShort(TupleObject target, String key, short defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toShort(StringUtils.of(target.get(key)), defValue);
	}

	public static double getDouble(TupleObject target, String key, double defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toDouble(StringUtils.of(target.get(key)), defValue);
	}

	public static boolean getBoolean(TupleObject target, String key, boolean defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return Boolean.parseBoolean(StringUtils.of(target.get(key)));
	}

	public static TupleObject getTupleObject(TupleObject target, String key) {
		if (target == null || !target.containsKey(key)) {
			return emptyObject();
		}
		try {
			return ClassUtils.cast(target.get(key), TupleObject.class);
		} catch (BaseException e) {
			return emptyObject();
		}
	}
}

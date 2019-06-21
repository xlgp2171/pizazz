package org.pizazz.common;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pizazz.common.ref.IKryoConfig;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.UtilityException;

/**
 * 通用对象工具
 * 
 * @author xlgp2171
 * @version 1.6.190617
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

	public static TupleObject newObject(Map<String, ? extends Object> map) {
		return new TupleObject(map);
	}

	public static boolean isEmpty(TupleObject target) {
		return target == null || target.isEmpty();
	}

	public static byte[] serialize(TupleObject target) throws AssertException, UtilityException {
		return SerializationUtils.serialize(target, new IKryoConfig() {
		});
	}

	public static TupleObject deserialize(byte[] target) throws AssertException, UtilityException {
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

	public static TupleObject merge(TupleObject left, TupleObject right) {
		if (left == null && right == null) {
			return emptyObject();
		} else if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			left.putAll(right);
			return left;
		}
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

	public static String getNestedString(TupleObject target, String defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getString(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getString(target, keys[keys.length - 1], defValue);
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

	public static int getNestedInt(TupleObject target, int defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getInt(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getInt(target, keys[keys.length - 1], defValue);
	}

	public static long getLong(TupleObject target, String key, long defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toLong(StringUtils.of(target.get(key)), defValue);
	}

	public static long getNestedLong(TupleObject target, long defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getLong(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getLong(target, keys[keys.length - 1], defValue);
	}

	public static short getShort(TupleObject target, String key, short defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toShort(StringUtils.of(target.get(key)), defValue);
	}

	public static short getNestedShort(TupleObject target, short defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getShort(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getShort(target, keys[keys.length - 1], defValue);
	}

	public static double getDouble(TupleObject target, String key, double defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toDouble(StringUtils.of(target.get(key)), defValue);
	}

	public static double getNestedDouble(TupleObject target, double defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getDouble(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getDouble(target, keys[keys.length - 1], defValue);
	}

	public static boolean getBoolean(TupleObject target, String key, boolean defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return Boolean.parseBoolean(StringUtils.of(target.get(key)));
	}

	public static boolean getNestedBoolean(TupleObject target, boolean defValue, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return defValue;
		}
		if (keys.length == 1) {
			return getBoolean(target, keys[0], defValue);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getBoolean(target, keys[keys.length - 1], defValue);
	}

	@SuppressWarnings("unchecked")
	public static TupleObject getTupleObject(TupleObject target, String key) {
		if (target == null || !target.containsKey(key)) {
			return emptyObject();
		}
		Object _item = target.get(key);
		try {
			return ClassUtils.cast(_item, TupleObject.class);
		} catch (BaseException e1) {
			TupleObject _tmp;
			try {
				_tmp = newObject(ClassUtils.cast(_item, Map.class));
				target.put(key, _tmp);
			} catch (BaseException e2) {
				_tmp = emptyObject();
			}
			return _tmp;
		}
	}

	public static TupleObject getNestedTupleObject(TupleObject target, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return emptyObject();
		}
		if (keys.length == 1) {
			return getTupleObject(target, keys[0]);
		}
		for (int _i = 0; _i < keys.length; _i++) {
			target = getTupleObject(target, keys[_i]);
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> getList(TupleObject target, String key) {
		if (target == null || !target.containsKey(key)) {
			return CollectionUtils.emptyList();
		}
		try {
			return ClassUtils.cast(target.get(key), List.class);
		} catch (BaseException e) {
			return CollectionUtils.emptyList();
		}
	}

	/**
	 * 按照key值顺序获取深层嵌套的List<br>
	 * 最后一个key采用getList方法取值
	 * 
	 * @param target 获取目标
	 * @param keys key值顺序
	 * @return List集合
	 */
	public static List<Object> getNestedList(TupleObject target, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return CollectionUtils.emptyList();
		}
		if (keys.length == 1) {
			return getList(target, keys[0]);
		}
		String[] _tmp = new String[keys.length - 1];
		System.arraycopy(keys, 0, _tmp, 0, keys.length - 1);
		target = getNestedTupleObject(target, _tmp);
		return getList(target, keys[keys.length - 1]);
	}

	public static TupleObject copy(TupleObject target, String... keys) {
		if (target == null || ArrayUtils.isEmpty(keys)) {
			return target.clone();
		}
		TupleObject _result = TupleObjectHelper.newObject(keys.length);

		for (String _item : keys) {
			Object _v = target.get(_item);
			_result.put(_item, _v);
		}
		return _result;
	}
}

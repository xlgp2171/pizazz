package org.pizazz2.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.pizazz2.Constant;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 资源工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ResourceUtils {

	public static Properties loadProperties(String resource) throws ValidateException, UtilityException {
		return ResourceUtils.loadProperties(resource, Constant.class, null, true);
	}

	public static Properties loadProperties(String resource, Class<?> clazz, Thread current, boolean close)
			throws ValidateException, UtilityException {
		InputStream _stream = IOUtils.getResourceAsStream(resource, clazz, current);
		return ResourceUtils.loadProperties(_stream, close);
	}

	public static Properties mergeProperties(Properties target, String resource)
			throws ValidateException, UtilityException {
		InputStream stream = IOUtils.getResourceAsStream(resource, Constant.class, null);
		return ResourceUtils.mergeProperties(stream, target, true);
	}

	public static Properties loadProperties(InputStream stream, boolean close)
			throws ValidateException, UtilityException {
		return ResourceUtils.mergeProperties(stream, null, close);
	}

	public static Properties mergeProperties(InputStream stream, Properties target, boolean close)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("mergeProperties", stream);
		Properties properties = new Properties();
		try {
			properties.load(stream);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "BASIC.ERR.IO.IN", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
		if (target != null) {
			target.putAll(properties);
		}
		try {
			return properties;
		} finally {
			if (close) {
				IOUtils.close(stream);
			}
		}
	}

	public static String getString(Properties target, String key, String defValue) {
		if (target == null) {
			return defValue;
		}
		return target.getProperty(key, defValue);
	}

	public static int getInt(Properties target, String key, int defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toInt(target.getProperty(key), defValue);
	}

	public static long getLong(Properties target, String key, long defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toLong(target.getProperty(key), defValue);
	}

	public static double getDouble(Properties target, String key, double defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toDouble(target.getProperty(key), defValue);
	}

	public static boolean getBoolean(Properties target, String key, boolean defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return BooleanUtils.toBoolean(target.getProperty(key), defValue);
	}

	public static String getString(Map<String, ?> target, String key, String defValue) {
		if (target != null && target.containsKey(key)) {
			return StringUtils.of(target.get(key));
		}
		return defValue;
	}

	public static int getInt(Map<String, ?> target, String key, int defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toInt(StringUtils.of(target.get(key)), defValue);
	}

	public static long getLong(Map<String, ?> target, String key, long defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toLong(StringUtils.of(target.get(key)), defValue);
	}

	public static double getDouble(Map<String, ?> target, String key, double defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return NumberUtils.toDouble(StringUtils.of(target.get(key)), defValue);
	}

	public static boolean getBoolean(Map<String, ?> target, String key, boolean defValue) {
		if (target == null || !target.containsKey(key)) {
			return defValue;
		}
		return BooleanUtils.toBoolean(StringUtils.of(target.get(key)), defValue);
	}
}

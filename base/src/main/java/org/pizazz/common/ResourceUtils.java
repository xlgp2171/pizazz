package org.pizazz.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pizazz.Constant;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 资源工具
 * 
 * @author xlgp2171
 * @version 1.1.190219
 */
public class ResourceUtils {

	public static Properties loadProperties(String resource) throws AssertException, UtilityException {
		return loadProperties(resource, Constant.class, null, true);
	}

	public static Properties loadProperties(String resource, Class<?> clazz, Thread current, boolean close)
			throws AssertException, UtilityException {
		InputStream _stream = IOUtils.getResourceAsStream(resource, clazz, current);
		return loadProperties(_stream, close);
	}

	/**
	 * @param target
	 * @param resource
	 * @return
	 * @throws UtilityException
	 * @throws AssertException
	 */
	public static Properties mergeProperties(Properties target, String resource)
			throws AssertException, UtilityException {
		InputStream _stream = IOUtils.getResourceAsStream(resource, Constant.class, null);
		return mergeProperties(_stream, target, true);
	}

	/**
	 * @param stream
	 * @param close
	 * @return
	 * @throws UtilityException
	 * @throws AssertException
	 */
	public static Properties loadProperties(InputStream stream, boolean close)
			throws AssertException, UtilityException {
		return mergeProperties(stream, null, close);
	}

	public static Properties mergeProperties(InputStream stream, Properties target, boolean close)
			throws AssertException, UtilityException {
		AssertUtils.assertNotNull("mergeProperties", stream);
		Properties _properties = new Properties();
		try {
			_properties.load(stream);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "BASIC.ERR.IO.IN", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		if (target != null) {
			target.putAll(_properties);
		}
		try {
			return _properties;
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
}

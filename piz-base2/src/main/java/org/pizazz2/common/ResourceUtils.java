package org.pizazz2.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.pizazz2.IObject;
import org.pizazz2.PizContext;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 资源工具
 * 
 * @author xlgp2171
 * @version 2.1.211028
 */
public class ResourceUtils {

	public static Properties loadProperties(String resource) throws ValidateException, UtilityException {
		return ResourceUtils.loadProperties(resource, PizContext.CLASS_LOADER, true);
	}

	public static Properties loadProperties(String resource, ClassLoader loader, boolean close)
			throws ValidateException, UtilityException {
		InputStream stream = IOUtils.getResourceAsStream(resource, loader);
		return ResourceUtils.loadProperties(stream, close);
	}

	public static Properties mergeProperties(Properties target, String resource)
			throws ValidateException, UtilityException {
		InputStream stream = IOUtils.getResourceAsStream(resource, PizContext.class, null);
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
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.IN", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
		if (target != null) {
			properties.putAll(target);
		}
		try {
			return properties;
		} finally {
			if (close) {
				SystemUtils.close(stream);
			}
		}
	}

	/**
	 * 将map转换为properties
	 * @param target 目标map
	 * @return properties对象
	 */
	public static Properties asProperties(Map<String, String> target) {
		Properties tmp = new Properties();

		if (!CollectionUtils.isEmpty(target)) {
			tmp.putAll(target);
		}
		return tmp;
	}

	/**
	 * 从资源中获取属性
	 * @param target 资源目标（实现IObject接口）
	 * @param key 资源对象对应的键
	 * @param sysKey 系统变量对应的键
	 * @param defValue 默认值
	 * @return 资源属性
	 * @throws ValidateException 验证异常
	 */
	public static String getProperty(IObject target, String key, String sysKey, String defValue)
			throws ValidateException {
		ValidateUtils.notNull("getString", target, key, sysKey);
		Object value = target.get(key, StringUtils.EMPTY);

		if (value == null || StringUtils.isTrimEmpty(StringUtils.of(value))) {
			value = SystemUtils.getSystemProperty(sysKey, defValue);
		}
		return StringUtils.of(value);
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

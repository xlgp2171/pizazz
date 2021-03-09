package org.pizazz2.helper;

import java.util.Properties;

import org.pizazz2.common.*;
import org.pizazz2.context.ConfigureContext;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.message.ref.IType;

/**
 * 内部配置工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ConfigureHelper {

	public static Properties validate(IType type) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		ConfigureContext.getInstance().register(type);
		return ConfigureContext.getInstance().getProperties(type);
	}

	public static String getString(IType type, String key, String defValue) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties properties = ConfigureHelper.validate(type);
		return ResourceUtils.getString(properties, type.toConfigureKey(key), defValue);
	}

	public static int getInt(IType type, String key, int defValue) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties properties = ConfigureHelper.validate(type);
		return ResourceUtils.getInt(properties, type.toConfigureKey(key), defValue);
	}

	public static long getLong(IType type, String key, long defValue) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties properties = ConfigureHelper.validate(type);
		return ResourceUtils.getLong(properties, type.toConfigureKey(key), defValue);
	}

	public static double getDouble(IType type, String key, double defValue) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties properties = ConfigureHelper.validate(type);
		return ResourceUtils.getDouble(properties, type.toConfigureKey(key), defValue);
	}

	public static boolean getBoolean(IType type, String key, boolean defValue) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties properties = ConfigureHelper.validate(type);
		return ResourceUtils.getBoolean(properties, type.toConfigureKey(key), defValue);
	}

	/**
	 * 获取应用设置参数
	 * <li/>根据key从type对应文件获取配置
	 * <li/>若获取成功再将此配置作为应用的key取值
	 * <li/>若获取失败再将此key作为应用的key取值
	 * 
	 * @param type 配置对应类型
	 * @param key 配置对应key
	 * @return 按照获取key从应用中获取对应值
	 */
	public static String getSystemProperty(IType type, String key) {
		String property = ConfigureHelper.getString(type, key, key);
		return SystemUtils.getSystemProperty(property, StringUtils.EMPTY);
	}

	public static String getConfig(IType type, String key, String defKey, String defValue) {
		String value = ConfigureHelper.getSystemProperty(type, key);

		if (StringUtils.isEmpty(value)) {
			value = ConfigureHelper.getString(type, defKey, defValue);
		}
		return value;
	}

	public static int getConfig(IType type, String key, String defKey, int defValue) {
		String value = ConfigureHelper.getSystemProperty(type, key);

		if (StringUtils.isEmpty(value)) {
			return ConfigureHelper.getInt(type, defKey, defValue);
		}
		return NumberUtils.toInt(value, defValue);
	}

	public static long getConfig(IType type, String key, String defKey, long defValue) {
		String value = ConfigureHelper.getSystemProperty(type, key);

		if (StringUtils.isEmpty(value)) {
			return ConfigureHelper.getLong(type, defKey, defValue);
		}
		return NumberUtils.toLong(value, defValue);
	}

	public static double getConfig(IType type, String key, String defKey, double defValue) {
		String value = ConfigureHelper.getSystemProperty(type, key);

		if (StringUtils.isEmpty(value)) {
			return ConfigureHelper.getDouble(type, defKey, defValue);
		}
		return NumberUtils.toDouble(value, defValue);
	}

	public static boolean getConfig(IType type, String key, String defKey, boolean defValue) {
		String value = ConfigureHelper.getSystemProperty(type, key);

		if (StringUtils.isEmpty(value)) {
			return ConfigureHelper.getBoolean(type, defKey, defValue);
		}
		return BooleanUtils.toBoolean(value, defValue);
	}
}

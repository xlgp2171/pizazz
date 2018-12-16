package org.pizazz.common;

import java.util.Properties;

import org.pizazz.context.ConfigureContext;
import org.pizazz.message.ref.IType;
import org.pizazz.message.ref.TypeEnum;

/**
 * 内部配置工具
 * 
 * @author xlgp2171
 * @version 1.1.181216
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
		return ResourceUtils.getString(validate(type), type.toConfigureKey(key), defValue);
	}

	public static int getInt(IType type, String key, int defValue) {
		return ResourceUtils.getInt(validate(type), type.toConfigureKey(key), defValue);
	}

	public static long getLong(IType type, String key, long defValue) {
		return ResourceUtils.getLong(validate(type), type.toConfigureKey(key), defValue);
	}

	public static double getDouble(IType type, String key, double defValue) {
		return ResourceUtils.getDouble(validate(type), type.toConfigureKey(key), defValue);
	}

	public static boolean getBoolean(IType type, String key, boolean defValue) {
		return ResourceUtils.getBoolean(validate(type), type.toConfigureKey(key), defValue);
	}

	/**
	 * 获取应用设置参数<br>
	 * 根据key从type对应文件获取配置<br>
	 * 若获取成功再将此配置作为应用的key取值<br>
	 * 若获取失败再将此key作为应用的key取值
	 * 
	 * @param type 配置对应类型
	 * @param key 配置对应key
	 * @return 按照获取key从应用中获取对应值
	 */
	public static String getSystemProperty(IType type, String key) {
		return SystemUtils.getSystemProperty(getString(type, key, key), StringUtils.EMPTY);
	}

	public static String getConfig(IType type, String key, String defKey, String defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			_value = getString(type, defKey, defValue);
		}
		return _value;
	}

	public static int getConfig(IType type, String key, String defKey, int defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getInt(type, defKey, defValue);
		}
		return NumberUtils.toInt(_value, defValue);
	}

	public static long getConfig(IType type, String key, String defKey, long defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getLong(type, defKey, defValue);
		}
		return NumberUtils.toLong(_value, defValue);
	}

	public static double getConfig(IType type, String key, String defKey, double defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getDouble(type, defKey, defValue);
		}
		return NumberUtils.toDouble(_value, defValue);
	}

	public static boolean getConfig(IType type, String key, String defKey, boolean defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getBoolean(type, defKey, defValue);
		}
		return BooleanUtils.toBoolean(_value, defValue);
	}
}

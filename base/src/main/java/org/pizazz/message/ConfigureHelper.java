package org.pizazz.message;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.pizazz.message.ref.IType;
import org.pizazz.message.ref.TypeEnum;
import org.pizazz.common.BooleanUtils;
import org.pizazz.common.NumberUtils;
import org.pizazz.Constant;
import org.pizazz.common.ResourceUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.BaseException;

/**
 * 内部配置工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class ConfigureHelper {

	static final Properties STORE = new Properties();
	static final Set<IType> SIGN = new HashSet<IType>();

	static {
		validate(TypeEnum.BASIC);
	}

	static IType validate(IType type) {
		if (type == null) {
			return TypeEnum.BASIC;
		}
		if (!SIGN.contains(type)) {
			synchronized (SIGN) {
				if (!SIGN.contains(type)) {
					load(type);
					SIGN.add(type);
				}
			}
		}
		return type;
	}

	static void load(IType type) {
		String _postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".configure.postfix", "_Configure");
		try {
			Properties _tmp = ResourceUtils.loadProperties(type.value() + _postfix + ".properties");
			STORE.putAll(_tmp);
		} catch (BaseException e) {
			SystemUtils.println(System.err, new StringBuilder(e.getMessage()));
		}
	}

	public static String getString(String key, String defValue) {
		return ResourceUtils.getString(STORE, key, defValue);
	}

	public static String getString(IType type, String key, String defValue) {
		return getString(validate(type).toConfigureKey(key), defValue);
	}

	public static int getInt(String key, int defValue) {
		return ResourceUtils.getInt(STORE, key, defValue);
	}

	public static int getInt(IType type, String key, int defValue) {
		return getInt(validate(type).toConfigureKey(key), defValue);
	}

	public static long getLong(String key, long defValue) {
		return ResourceUtils.getLong(STORE, key, defValue);
	}

	public static long getLong(IType type, String key, long defValue) {
		return getLong(validate(type).toConfigureKey(key), defValue);
	}

	public static double getDouble(String key, double defValue) {
		return ResourceUtils.getDouble(STORE, key, defValue);
	}

	public static double getDouble(IType type, String key, double defValue) {
		return getDouble(validate(type).toConfigureKey(key), defValue);
	}

	public static boolean getBoolean(String key, boolean defValue) {
		return ResourceUtils.getBoolean(STORE, key, defValue);
	}

	public static boolean getBoolean(IType type, String key, boolean defValue) {
		return getBoolean(validate(type).toConfigureKey(key), defValue);
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
		String _key = validate(type).toConfigureKey(key);
		return SystemUtils.getSystemProperty(getString(_key, key), StringUtils.EMPTY);
	}

	public static String getConfig(IType type, String key, String defKey, String defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			_value = getString(validate(type).toConfigureKey(defKey), defValue);
		}
		return _value;
	}

	public static int getConfig(IType type, String key, String defKey, int defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getInt(validate(type).toConfigureKey(defKey), defValue);
		}
		return NumberUtils.toInt(_value, defValue);
	}

	public static long getConfig(IType type, String key, String defKey, long defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getLong(validate(type).toConfigureKey(defKey), defValue);
		}
		return NumberUtils.toLong(_value, defValue);
	}

	public static double getConfig(IType type, String key, String defKey, double defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getDouble(validate(type).toConfigureKey(defKey), defValue);
		}
		return NumberUtils.toDouble(_value, defValue);
	}

	public static boolean getConfig(IType type, String key, String defKey, boolean defValue) {
		String _value = getSystemProperty(type, key);

		if (StringUtils.isEmpty(_value)) {
			return getBoolean(validate(type).toConfigureKey(defKey), defValue);
		}
		return BooleanUtils.toBoolean(_value, defValue);
	}
}

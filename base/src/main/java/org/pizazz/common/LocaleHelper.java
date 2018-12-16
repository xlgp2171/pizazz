package org.pizazz.common;

import java.util.Locale;
import java.util.Properties;

import org.pizazz.context.LocaleContext;
import org.pizazz.message.ref.IType;
import org.pizazz.message.ref.TypeEnum;

/**
 * 国际化消息组件
 * 
 * @author xlgp2171
 * @version 1.1.181216
 */
public class LocaleHelper {

	public static Properties validate(IType type, Locale locale) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		if (locale == null) {
			locale = SystemUtils.LOCAL_LOCALE;
		}
		LocaleContext.getInstance().register(type, locale);
		return LocaleContext.getInstance().getProperties(type, locale);
	}

	/**
	 * 根据消息类型获取提示信息
	 * 
	 * @param type
	 * @param key
	 * @param arguments
	 * @return
	 */
	public static String toLocaleText(IType type, String key, Object... arguments) {
		return toLocaleText(type, SystemUtils.LOCAL_LOCALE, key, arguments);
	}

	public static String toLocaleText(IType type, Locale locale, String key, Object... arguments) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties _tmp = validate(type, locale);
		String _key = type.toLocaleKey(key);
		String _result = ResourceUtils.getString(_tmp, _key, _key);
		return StringUtils.format(_result, arguments);
	}
}

package org.pizazz2.helper;

import java.util.Locale;
import java.util.Properties;

import org.pizazz2.PizContext;
import org.pizazz2.common.ResourceUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.context.LocaleContext;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.message.ref.IType;

/**
 * 国际化消息组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class LocaleHelper {

	public static Properties validate(IType type, Locale locale) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		if (locale == null) {
			locale = PizContext.LOCAL_LOCALE;
		}
		return LocaleContext.getInstance().register(type, locale).getProperties(type, locale);
	}

	/**
	 * 根据消息类型获取提示信息
	 * 
	 * @param type 消息类型
	 * @param key 消息对应key
	 * @param arguments 消息中的参数
	 * @return 消息字符串
	 */
	public static String toLocaleText(IType type, String key, Object... arguments) {
		return LocaleHelper.toLocaleText(type, PizContext.LOCAL_LOCALE, key, arguments);
	}

	public static String toLocaleText(IType type, Locale locale, String key, Object... arguments) {
		if (type == null) {
			type = TypeEnum.BASIC;
		}
		Properties tmp = LocaleHelper.validate(type, locale);
		String localKey = type.toLocaleKey(key);
		String result = ResourceUtils.getString(tmp, localKey, localKey);
		return StringUtils.format(result, arguments);
	}
}

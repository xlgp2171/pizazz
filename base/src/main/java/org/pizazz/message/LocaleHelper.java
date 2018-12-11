package org.pizazz.message;

import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.pizazz.message.ref.IType;
import org.pizazz.message.ref.TypeEnum;
import org.pizazz.Constant;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;

/**
 * 国际化消息组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class LocaleHelper {

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
		String _postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".locale.postfix", "_Locale");
		ResourceBundle _resource = ResourceBundle.getBundle(type.value() + _postfix, SystemUtils.LOCAL_LOCALE);
		_resource.keySet().stream().forEach(_item -> STORE.setProperty(_item, _resource.getString(_item)));
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
		key = validate(type).toLocaleKey(key);

		if (STORE.containsKey(key)) {
			key = STORE.getProperty(key, key);
		}
		return StringUtils.format(key, arguments);
	}
}

package org.pizazz.common;

/**
 * 布尔工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class BooleanUtils {

	public static boolean toBoolean(String target, boolean defValue) {
		if (target == null) {
			return defValue;
		}
		return Boolean.parseBoolean(target);
	}
}

package org.pizazz.common;

/**
 * 布尔工具
 * 
 * @author xlgp2171
 * @version 1.0.190624
 */
public class BooleanUtils {

	public static boolean toBoolean(String target, boolean defValue) {
		if (target == null) {
			return defValue;
		}
		if ("1".equals(target)) {
			return Boolean.TRUE;
		}
		return Boolean.parseBoolean(target);
	}
}

package org.pizazz2.common;

import org.pizazz2.exception.ValidateException;

/**
 * 布尔工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class BooleanUtils {
	/**
	 * 字符串转换工具
	 * <li/>若为字符串"1"也会识别为true
	 * @param target 转换参数
	 * @param defValue 默认值
	 * @return 转换后的值
	 */
	public static boolean toBoolean(String target, boolean defValue) {
		if (target == null) {
			return defValue;
		} else if (NumberUtils.ONE.toString().equals(target)) {
			return Boolean.TRUE;
		}
		return Boolean.parseBoolean(target);
	}

	/**
	 * 字符串转换工具
	 * <li/>若为字符串"1"也会识别为true
	 * @param target 转换参数
	 * @return 转换后的值
	 * @throws ValidateException 验证异常
	 */
	public static boolean toBoolean(String target) throws ValidateException {
		ValidateUtils.notNull("toBoolean", target);
		return BooleanUtils.toBoolean(target, Boolean.TRUE);
	}
}

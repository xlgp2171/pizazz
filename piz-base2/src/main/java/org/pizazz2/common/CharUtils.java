package org.pizazz2.common;

import org.pizazz2.exception.ValidateException;

/**
 * 字符工具
 * 
 * @author xlgp2171
 * @version 2.1.220707
 */
public class CharUtils {
	public static final String MAGIC_UNICODE = "\\u";
	public static final char UNDERLINE = '_';

	public static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	/**
	 * 转换为unicode值
	 *
	 * @param ch 字符
	 * @return unicode值
	 */
	public static String toUnicodeValue(char ch) {
		return StringUtils.of(HEX_DIGITS[(ch >> 12) & 15]) + HEX_DIGITS[(ch >> 8) & 15] + HEX_DIGITS[(ch >> 4) & 15] +
				HEX_DIGITS[(ch) & 15];
	}

	public static char toChar(String target) throws ValidateException {
		return CharUtils.toChar(target, 16);
	}

	public static char toChar(String target, int radix) throws ValidateException {
		ValidateUtils.notEmpty("toChar", target, 1);

		if (target.startsWith(MAGIC_UNICODE)) {
			target = target.substring(2);
		}
		return (char) NumberUtils.toInt(target, radix, 0);
	}

	public static String fromUnicode(String first, String... more) throws ValidateException {
		ValidateUtils.sameLength("fromUnicode", 1, first, 4);
		StringBuilder tmp = new StringBuilder().append(CharUtils.toChar(first, 16));

		if (more == null) {
			return tmp.toString();
		}
		for (int i = 0; i < more.length; i ++) {
			ValidateUtils.sameLength("fromUnicode", i + 1, more[i], 4);
			tmp.append(CharUtils.toChar(more[i], 16));
		}
		return tmp.toString();
	}
}

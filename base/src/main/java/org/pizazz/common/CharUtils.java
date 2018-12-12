package org.pizazz.common;

import org.pizazz.exception.BaseException;

/**
 * 字符工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class CharUtils {
	public static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	public static String toUnicodeValue(char ch) {
		return new StringBuilder(4).append(HEX_DIGITS[(ch >> 12) & 15]).append(HEX_DIGITS[(ch >> 8) & 15])
				.append(HEX_DIGITS[(ch >> 4) & 15]).append(HEX_DIGITS[(ch) & 15]).toString();
	}

	public static char toChar(String target) {
		return toChar(target, 16);
	}

	public static char toChar(String target, int radix) {
		int _tmp = NumberUtils.toInt(target, radix, 0);
		return (char) _tmp;
	}

	public static String fromUnicode(String first, String... more) throws BaseException {
		AssertUtils.assertLength("fromUnicode", 1, first, 4);
		StringBuilder _tmp = new StringBuilder().append(toChar(first, 16));

		if (more == null) {
			return _tmp.toString();
		}
		for (int _i = 0; _i < more.length; _i ++) {
			AssertUtils.assertLength("fromUnicode", _i + 1, more[_i], 4);
			_tmp.append(toChar(more[_i], 16));
		}
		return _tmp.toString();
	}
}

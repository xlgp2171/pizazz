package org.pizazz.common;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pizazz.exception.BaseException;

/**
 * 字符串对象工具<br>
 * 部分参考org.apache.commons.lang3.StringUtils
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class StringUtils {

	public static final String SPACE = " ";
	public static final String EMPTY = "";

	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isTrimEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isBlank(CharSequence cs) {
		int _length;

		if (cs == null || (_length = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < _length; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String format(String pattern, Object... arguments) {
		if (pattern == null) {
			return StringUtils.EMPTY;
		}
		try {
			return MessageFormat.format(pattern, arguments);
		} catch (IllegalArgumentException e) {
			return pattern;
		}
	}

	public static String toHexString(byte[] bytes, String separator) {
		StringBuilder _tmp = new StringBuilder(bytes.length * 3);

		if (ArrayUtils.isEmpty(bytes)) {
			return StringUtils.EMPTY;
		}
		append(bytes[0], _tmp);

		for (int _i = 1; _i < bytes.length; _i++) {
			append(bytes[_i], _tmp.append(separator));
		}
		return _tmp.toString();
	}

	private static void append(int target, StringBuilder sb) {
		int _item = target;
		_item &= 0xff;
		sb.append(CharUtils.HEX_DIGITS[_item >> 4]);
		sb.append(CharUtils.HEX_DIGITS[_item & 15]);
	}

	public static String join(Object[] arguments, String separator) {
		if (arguments == null || arguments.length == 0) {
			return "";
		}
		StringBuilder _tmp = new StringBuilder(String.valueOf(arguments[0]));

		for (int _i = 1; _i < arguments.length; _i++) {
			_tmp.append(separator).append(arguments[_i]);
		}
		return _tmp.toString();
	}

	public static String toString(String str, int repeat) {
		StringBuilder _tmp = new StringBuilder();

		for (int _i = 0; _i < repeat; _i++) {
			_tmp.append(str);
		}
		return _tmp.toString();
	}

	/**
	 * 以style字符串内容在target字符串的prior=true(前)/false(后)补足<br>
	 * 例如:123,ABCDE,true=AB123;123,ABCDE,false=123DE
	 * 
	 * @param target 需补足的字符串
	 * @param style 样式字符串
	 * @param prior 前true/后false
	 * @return
	 */
	public static String fillAndReplace(String target, String style, boolean prior) {
		if (target == null && style != null) {
			return style;
		} else if (target != null && style == null) {
			return target;
		} else if (target == null && style == null) {
			return EMPTY;
		}
		int _index = target.length();
		int _length = style.length();

		if (_length <= _index) {
			return target;
		}
		if (prior) {
			return style.substring(0, _length - _index) + target;
		} else {
			return target + style.substring(_index);
		}
	}

	public static String toPath(boolean relatively, String... elements) {
		if (ArrayUtils.isEmpty(elements)) {
			return EMPTY;
		}
		String[] _elements = elements;
		StringBuilder _path = new StringBuilder();

		for (String _element : _elements) {
			_path.append("/").append(_element);
		}
		return relatively ? _path.substring(1) : _path.toString();
	}

	public static String match(Pattern pattern, String input, int group) throws BaseException {
		AssertUtils.assertNotNull("match", pattern, input);
		Matcher _matcher = pattern.matcher(input);

		if (_matcher.find()) {
			if (group < 0 || group > _matcher.groupCount()) {
				return null;
			}
			return _matcher.group(group);
		}
		return "";
	}

	public static String match(String regex, String input, int group) throws BaseException {
		AssertUtils.assertNotNull("match", regex);
		return match(Pattern.compile(regex), input, group);
	}

	public static String capitalize(String target) {
		return isTrimEmpty(target) ? EMPTY : Character.toTitleCase(target.charAt(0)) + target.substring(1);
	}

	public static String of(Object target) {
		return String.valueOf(target);
	}
}

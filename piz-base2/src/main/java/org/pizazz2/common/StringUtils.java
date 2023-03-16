package org.pizazz2.common;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pizazz2.exception.ValidateException;
import org.pizazz2.message.ExpressionEnum;

/**
 * 字符串对象工具<br>
 * 部分参考org.apache.commons.lang3.StringUtils
 * 部分参考com.baomidou.mybatisplus.core.toolkit.StringUtils
 * 
 * @author xlgp2171
 * @version 2.2.230315
 */
public class StringUtils {

	public static final String SPACE = " ";
	public static final String EMPTY = "";

	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isNotEmpty(CharSequence cs) {
		return !StringUtils.isEmpty(cs);
	}

	public static boolean isTrimEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isBlank(CharSequence cs) {
		int length;

		if (cs == null || (length = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String nullToEmpty(String target) {
		return StringUtils.isTrimEmpty(target) ? StringUtils.EMPTY : target;
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

	/**
	 * 转换为十六进制字符串
	 * @param bytes 数据源
	 * @param separator 分隔字符串
	 * @return 十六进制字符串
	 */
	public static String toHexString(byte[] bytes, String separator) {
		StringBuilder tmp = new StringBuilder(bytes.length * 3);

		if (ArrayUtils.isEmpty(bytes)) {
			return StringUtils.EMPTY;
		}
		StringUtils.append(bytes[0], tmp);

		for (int i = 1; i < bytes.length; i++) {
			StringUtils.append(bytes[i], tmp.append(separator));
		}
		return tmp.toString();
	}

	private static void append(int target, StringBuilder sb) {
		int item = target;
		item &= 0xff;
		sb.append(CharUtils.HEX_DIGITS[item >> 4]);
		sb.append(CharUtils.HEX_DIGITS[item & 15]);
	}

	/**
	 * 连接字符串<br>
	 * 如：参数[A,B,C,D],-为A-B-C-D
	 * @param arguments 字符串组
	 * @param separator 分隔字符
	 * @return 以分隔字符串连接字符串组
	 */
	public static String join(Object[] arguments, String separator) {
		if (arguments == null || arguments.length == 0) {
			return "";
		}
		StringBuilder tmp = new StringBuilder(String.valueOf(arguments[0]));

		for (int i = 1; i < arguments.length; i++) {
			tmp.append(separator).append(arguments[i]);
		}
		return tmp.toString();
	}

	/**
	 * 重复字符串
	 * @param str 重复字符串
	 * @param repeat 重复次数
	 * @return 满足重复次数的字符串
	 */
	public static String repeatString(String str, int repeat) {
		StringBuilder tmp = new StringBuilder();

		for (int i = 0; i < repeat; i++) {
			tmp.append(str);
		}
		return tmp.toString();
	}

	/**
	 * 以style字符串内容在target字符串的prior=true(前)/false(后)替换<br>
	 * 例如:
	 * <li/>参数123,ABCDE,true返回AB123;
	 * <li/>参数123,ABCDE,false返回123DE
	 * 
	 * @param target 需补足的字符串
	 * @param style 样式字符串
	 * @param prior 前true/后false
	 * @return 替换后的字符串
	 */
	public static String fillAndReplace(String target, String style, boolean prior) {
		if (target == null && style != null) {
			return style;
		} else if (target != null && style == null) {
			return target;
		} else if (target == null) {
			return EMPTY;
		}
		int index = target.length();
		int length = style.length();

		if (length <= index) {
			return target;
		}
		if (prior) {
			return style.substring(0, length - index) + target;
		} else {
			return target + style.substring(index);
		}
	}

	/**
	 * 转换为路径字符串<br>
	 * 以“/”隔开
	 * @param relatively 是否相对路径
	 * @param elements 路径元素
	 * @return 路径字符串
	 */
	public static String toPath(boolean relatively, String... elements) {
		if (ArrayUtils.isEmpty(elements)) {
			return EMPTY;
		}
		StringBuilder path = new StringBuilder();

		for (String element : elements) {
			path.append("/").append(element);
		}
		return relatively ? path.substring(1) : path.toString();
	}

	public static String match(Pattern pattern, String input, int group) throws ValidateException {
		ValidateUtils.notNull("match", pattern, input);
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			if (group < 0 || group > matcher.groupCount()) {
				return StringUtils.EMPTY;
			}
			return matcher.group(group);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 提取正则表达式匹配的字符串
	 * @param regex 正则表达式
	 * @param input 输入源字符串
	 * @param group 提取匹配的第几个
	 * @return 匹配的字符串
	 * @throws ValidateException 验证异常
	 */
	public static String match(String regex, String input, int group) throws ValidateException {
		ValidateUtils.notNull("match", regex);
		return StringUtils.match(Pattern.compile(regex), input, group);
	}

	/**
	 * 字符串首字母以大写字母写
	 * @param target 输入字符串
	 * @return 变换后的字符串
	 */
	public static String capitalize(String target) {
		return StringUtils.isTrimEmpty(target) ? EMPTY : Character.toTitleCase(target.charAt(0)) + target.substring(1);
	}

	/**
	 * 字符串驼峰转下划线格式
	 * @param target 需要转换的目标字符串
	 * @return 转换完成的字符串
	 */
	public static String camelToUnderline(String target) {
		if (StringUtils.isTrimEmpty(target)) {
			return StringUtils.EMPTY;
		}
		int length = target.length();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			char c = target.charAt(i);

			if (Character.isUpperCase(c) && i > 0) {
				sb.append(CharUtils.UNDERLINE);
			}
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}

	/**
	 * 字符串下划线转驼峰格式
	 * @param target 需要转换的目标字符串
	 * @return 转换完成的字符串
	 */
	public static String underlineToCamel(String target) {
		if (StringUtils.isTrimEmpty(target)) {
			return StringUtils.EMPTY;
		}
		String temp = target.toLowerCase();
		int length = temp.length();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			char c = temp.charAt(i);

			if (c == CharUtils.UNDERLINE) {
				if (++i < length) {
					sb.append(Character.toUpperCase(temp.charAt(i)));
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 字符串去除空白内容： \n 回车 \t 水平制表符 \s 空格 \r 换行
	 * @param target 目标字符串
	 * @return 处理后的字符串
	 */
	public static String replaceBlank(String target) {
		if (StringUtils.isEmpty(target)) {
			return StringUtils.EMPTY;
		}
		Pattern pattern = ExpressionEnum.getPattern(ExpressionEnum.BLANK);
		return pattern.matcher(target).replaceAll(StringUtils.EMPTY);
	}

	public static String of(Object target) {
		return target == null ? StringUtils.EMPTY : String.valueOf(target);
	}
}

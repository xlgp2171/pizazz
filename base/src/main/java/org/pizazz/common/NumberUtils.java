package org.pizazz.common;

import java.math.BigDecimal;

import org.pizazz.exception.AssertException;

/**
 * 数值工具
 * 
 * @author xlgp2171
 * @version 1.1.190709
 */
public class NumberUtils {
	/** int判断最大位数 */
	public static final int PRECISION_INT = 9;
	/** 0 */
	public static final Number ZERO = 0;
	/** 1 */
	public static final Number ONE = 1;

	public static int toInt(String target, int defValue) {
		return toInt(target, 10, defValue);
	}

	public static int toInt(String target, int radix, int defValue) {
		try {
			return Integer.parseInt(target, radix);
		} catch (NumberFormatException e) {
			return defValue;
		}
	}

	public static long toLong(String target, long defValue) {
		return toLong(target, 10, defValue);
	}

	public static long toLong(String target, int radix, long defValue) {
		try {
			return Long.parseLong(target, radix);
		} catch (NumberFormatException e) {
			return defValue;
		}
	}

	public static short toShort(String target, short defValue) {
		return toShort(target, 10, defValue);
	}

	public static short toShort(String target, int radix, short defValue) {
		try {
			return Short.parseShort(target, radix);
		} catch (NumberFormatException e) {
			return defValue;
		}
	}

	public static double toDouble(String target, double defValue) {
		try {
			return Double.parseDouble(target);
		} catch (NumberFormatException e) {
			return defValue;
		}
	}

	public static Number valueOf(BigDecimal target) throws AssertException {
		AssertUtils.assertNotNull("valueOf", target);

		if (target.scale() == 0) {
			return target.precision() > PRECISION_INT ? target.longValue() : target.intValue();
		} else {
			return target.doubleValue();
		}
	}

	public static String toPlainString(double target) {
		return new BigDecimal(target).toPlainString();
	}

	public static String toPlainString(double target, int scale) {
		return new BigDecimal(target).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
	}

	public static double round(double target, int scale) {
		return new BigDecimal(target).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static int random(int min, int max) {
		if (min >= max) {
			return max;
		}
		return min + new Double(Math.random() * max).intValue();
	}
}

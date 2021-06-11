package org.pizazz2.common;

import java.math.BigDecimal;

import org.pizazz2.exception.ValidateException;

/**
 * 数值工具
 *
 * @author xlgp2171
 * @version 2.0.210610
 */
public class NumberUtils {
    /**
     * int判断最大位数
     */
    public static final int PRECISION_INT = 9;
    /**
     * 0
     */
    public static final Number ZERO = 0;
    /**
     * 1
     */
    public static final Number ONE = 1;

    /**
     * 验证长度大小
     *
     * @param length 数组长度
     * @param min 为null则不验证
     * @param max 为null则不验证
     * @return 数组是否通过验证
     */
    public static boolean validate(long length, Long min, Long max) {
        if (min != null && length < min) {
            return false;
        }
        return !(max != null && length > max);
    }

    public static int toInt(String target, int defValue) {
        return NumberUtils.toInt(target, 10, defValue);
    }

    public static int toInt(String target, int radix, int defValue) {
        try {
            return Integer.parseInt(target, radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static long toLong(String target, long defValue) {
        return NumberUtils.toLong(target, 10, defValue);
    }

    public static long toLong(String target, int radix, long defValue) {
        try {
            return Long.parseLong(target, radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static short toShort(String target, short defValue) {
        return NumberUtils.toShort(target, 10, defValue);
    }

    public static short toShort(String target, int radix, short defValue) {
        try {
            return Short.parseShort(target, radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static byte toByte(String target, byte defValue) {
        return NumberUtils.toByte(target, 10, defValue);
    }

    public static byte toByte(String target, int radix, byte defValue) {
        try {
            return Byte.parseByte(target, radix);
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

    public static float toFloat(String target, float defValue) {
        try {
            return Float.parseFloat(target);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static Number valueOf(BigDecimal target) throws ValidateException {
        ValidateUtils.notNull("valueOf", target);

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

    public static Double round(double target, int scale) {
        return new BigDecimal(target).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Long random(long min, long max) {
        if (min >= max) {
            return max;
        }
        return min + new Double(Math.random() * (max - min)).longValue();
    }

    public static Double random(double min, double max) {
        if (min >= max) {
            return max;
        }
        return min + Math.random() * (max - min);
    }
}

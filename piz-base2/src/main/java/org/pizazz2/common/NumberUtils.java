package org.pizazz2.common;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.pizazz2.exception.ValidateException;

/**
 * 数值工具
 *
 * @author xlgp2171
 * @version 2.2.230315
 */
public class NumberUtils {
    /** 0 */
    public static final Number ZERO = 0;
    /** 1 */
    public static final Number ONE = 1;
    /** -1 */
    public static final Number NEGATIVE_ONE = -1;
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

    public static int toInt(Object target, int defValue) {
        return NumberUtils.toInt(target, 10, defValue);
    }

    public static int toInt(Object target, int radix, int defValue) {
        try {
            return Integer.parseInt(StringUtils.of(target), radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static long toLong(Object target, long defValue) {
        return NumberUtils.toLong(target, 10, defValue);
    }

    public static long toLong(Object target, int radix, long defValue) {
        try {
            return Long.parseLong(StringUtils.of(target), radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static short toShort(Object target, short defValue) {
        return NumberUtils.toShort(target, 10, defValue);
    }

    public static short toShort(Object target, int radix, short defValue) {
        try {
            return Short.parseShort(StringUtils.of(target), radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static byte toByte(Object target, byte defValue) {
        return NumberUtils.toByte(target, 10, defValue);
    }

    public static byte toByte(Object target, int radix, byte defValue) {
        try {
            return Byte.parseByte(StringUtils.of(target), radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static double toDouble(Object target, double defValue) {
        try {
            return Double.parseDouble(StringUtils.of(target));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static float toFloat(Object target, float defValue) {
        try {
            return Float.parseFloat(StringUtils.of(target));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static Class<?> getType(BigDecimal target) throws ValidateException {
        ValidateUtils.notNull("valueOf", target);

        if (target.scale() == 0) {
            long tmp;
            try {
                tmp = target.longValueExact();
            } catch (ArithmeticException e) {
                return BigInteger.class;
            }
            if (tmp <= Byte.MAX_VALUE) {
                return Byte.TYPE;
            } else if (tmp <= Short.MAX_VALUE) {
                return Short.TYPE;
            } else if (tmp <= Integer.MAX_VALUE) {
                return Integer.TYPE;
            } else {
                return Long.TYPE;
            }
        } else {
            return Double.TYPE;
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

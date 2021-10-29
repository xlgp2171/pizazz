package org.pizazz2.common;

import org.pizazz2.exception.ValidateException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数组工具
 *
 * @author xlgp2171
 * @version 2.1.211014
 */
public class ArrayUtils {

    public static final Object[] EMPTY_OBJECT = new Object[0];
    public static final String[] EMPTY_STRING = new String[0];
    public static final Class<?>[] EMPTY_CLASS = new Class<?>[0];
    public static final long[] EMPTY_LONG = new long[0];
    public static final int[] EMPTY_INT = new int[0];
    public static final short[] EMPTY_SHORT = new short[0];
    public static final byte[] EMPTY_BYTE = new byte[0];
    public static final double[] EMPTY_DOUBLE = new double[0];
    public static final float[] EMPTY_FLOAT = new float[0];
    public static final char[] EMPTY_CHAR = new char[0];
    public static final boolean[] EMPTY_BOOLEAN = new boolean[0];

    public static boolean isEmpty(Object[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(long[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(int[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(short[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(byte[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(double[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(float[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(char[] target) {
        return target == null || target.length == 0;
    }

    public static boolean isEmpty(boolean[] target) {
        return target == null || target.length == 0;
    }

    public static Object[] insert(Object[] target, int index, Object element) {
        if (target == null) {
            return EMPTY_OBJECT;
        }
        Object[] tmp = new Object[target.length + 1];
        int length = target.length;
		// 若序列小于等于0,则添加到第一个
        if (index <= 0) {
            System.arraycopy(target, 0, tmp, 1, length);
            tmp[0] = element;
        } else if (index >= length) {
            System.arraycopy(target, 0, tmp, 0, length);
            tmp[length] = element;
        } else {
            System.arraycopy(target, 0, tmp, 0, index);
            System.arraycopy(target, index, tmp, index + 1, length - index);
            tmp[index] = element;
        }
        return tmp;
    }

    public static boolean contains(Object[] target, Object o) {
        if (target != null) {
			for (Object item : target) {
				if (item != null && item.equals(o)) {
					return true;
				}
			}
        }
        return false;
    }

    public static byte[] nullToEmpty(byte[] target) {
        return ArrayUtils.isEmpty(target) ? ArrayUtils.EMPTY_BYTE : target;
    }

    public static Object[] nullToEmpty(Object[] target) {
        return ArrayUtils.isEmpty(target) ? ArrayUtils.EMPTY_OBJECT : target;
    }

	public static String[] nullToEmpty(String[] target) {
		return ArrayUtils.isEmpty(target) ? ArrayUtils.EMPTY_STRING : target;
	}

    public static Class<?>[] nullToEmpty(Class<?>[] target) {
		return ArrayUtils.isEmpty(target) ? ArrayUtils.EMPTY_CLASS : target;
    }

    public static String[] newArray(int size, String fill) {
        size = Math.max(NumberUtils.ONE.intValue(), size);
        String[] tmp = new String[size];
        Arrays.fill(tmp, fill);
        return tmp;
    }

    public static String[] merge(String[] left, String[] right) {
        if (right != null && isEmpty(left)) {
            return right;
        } else if (left != null && isEmpty(right)) {
            return left;
        } else if (isEmpty(left) && isEmpty(right)) {
            return EMPTY_STRING;
        }
        String[] tmp = new String[left.length + right.length];
        System.arraycopy(left, 0, tmp, 0, left.length);
        System.arraycopy(right, 0, tmp, left.length, right.length);
        return tmp;
    }

    /**
     * 将数组转换为ArrayList
     * @param target 数组对象
     * @param <T> 统一的对象类型
     * @return List对象
     * @throws ValidateException 验证异常
     */
    @SafeVarargs
    public static <T> List<T> asList(T... target) throws ValidateException {
        ValidateUtils.notNull("asList", (Object) target);
        return Stream.of(target).collect(Collectors.toList());
    }

    /**
     * 将数组转换为HashSet
     * @param target 数组对象
     * @param <T> 统一的对象类型
     * @return Set对象
     * @throws ValidateException 验证异常
     */
    @SafeVarargs
    public static <T> Set<T> asSet(T... target) throws ValidateException {
        ValidateUtils.notNull("asSet", (Object) target);
        return Stream.of(target).collect(Collectors.toSet());
    }

    public static int getMinimumLength(String[] target) {
        if (ArrayUtils.isEmpty(target)) {
            return NumberUtils.NEGATIVE_ONE.intValue();
        }
        int tmp = Integer.MAX_VALUE;

        for (String item : target) {
            if (item == null) {
                continue;
            }
            tmp = Math.min(tmp, item.length());
        }
        return tmp == Integer.MAX_VALUE ? NumberUtils.NEGATIVE_ONE.intValue() : tmp;
    }
}

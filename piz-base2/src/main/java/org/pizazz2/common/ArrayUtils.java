package org.pizazz2.common;

import java.util.Arrays;

/**
 * 数组工具
 *
 * @author xlgp2171
 * @version 2.0.210201
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

    public static Object[] nullToEmpty(Object[] target) {
        return isEmpty(target) ? EMPTY_OBJECT : target;
    }

	public static String[] nullToEmpty(String[] target) {
		return isEmpty(target) ? EMPTY_STRING : target;
	}

    public static Class<?>[] nullToEmpty(Class<?>[] target) {
		return isEmpty(target) ? EMPTY_CLASS : target;
    }

    public static String[] newArray(int size, String fill) {
        size = size < 0 ? 1 : size;
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
}

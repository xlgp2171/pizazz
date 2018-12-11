package org.pizazz.common;

import java.util.Arrays;

/**
 * 数组工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class ArrayUtils {

	public static final Object[] EMPTY_OBJECT = new Object[0];
	public static final Class<?>[] EMPTY_CLASS = new Class<?>[0];
	public static final String[] EMPTY_STRING = new String[0];
	public static final byte[] EMPTY_BYTE = new byte[0];
	public static final char[] EMPTY_CHAR = new char[0];
	public static final short[] EMPTY_SHORT = new short[0];
	public static final int[] EMPTY_INT = new int[0];
	public static final float[] EMPTY_FLOAT = new float[0];
	public static final double[] EMPTY_DOUBLE = new double[0];
	public static final long[] EMPTY_LONG = new long[0];

	public static boolean isEmpty(Object[] target) {
		return target == null || target.length == 0;
	}

	public static boolean isEmpty(byte[] target) {
		return target == null || target.length == 0;
	}

	public static boolean isEmpty(int[] target) {
		return target == null || target.length == 0;
	}

	/**
	 * 验证数组大小
	 * @param target
	 * @param min 为null则不验证
	 * @param max 为null则不验证
	 * @return
	 */
	public static boolean validate(Object[] target, Integer min, Integer max) {
		if (isEmpty(target)) {
			return false;
		}
		boolean _result = true;

		if (min != null) {
			_result = target.length >= min;
		}
		if (!_result) {
			return false;
		} else if (max != null) {
			_result = target.length <= max;
		}
		return _result;
	}

	/**
	 * 验证数组大小
	 * @param target
	 * @param min 为null则不验证
	 * @param max 为null则不验证
	 * @return
	 */
	public static boolean validate(byte[] target, Integer min, Integer max) {
		if (isEmpty(target)) {
			return false;
		}
		boolean _result = true;

		if (min != null) {
			_result = target.length >= min;
		}
		if (!_result) {
			return false;
		} else if (max != null) {
			_result = target.length <= max;
		}
		return _result;
	}

	public static Object[] insert(Object[] target, int index, Object element) {
		Object[] _tmp = new Object[target.length + 1];
		int _length = target.length;

		if (index <= 0) {
			System.arraycopy(target, 0, _tmp, 1, _length);
			_tmp[0] = element;
		} else if (index >= _length) {
			System.arraycopy(target, 0, _tmp, 0, _length);
			_tmp[_length] = element;
		} else {
			System.arraycopy(target, 0, _tmp, 0, index);
			System.arraycopy(target, index - 1, _tmp, index, _length - index);
			_tmp[index] = element;
		}
		return _tmp;
	}

	public static Object[] nullToEmpty(Object[] target) {
		if (isEmpty(target)) {
			return EMPTY_OBJECT;
		}
		return target;
	}

	public static Class<?>[] nullToEmpty(Class<?>[] target) {
		if (isEmpty(target)) {
			return EMPTY_CLASS;
		}
		return target;
	}

	public static String[] newArray(int size, String fill) {
		size = size < 0 ? 1 : size;
		String[] _tmp = new String[size];
		Arrays.fill(_tmp, fill);
		return _tmp;
	}

	public static String[] merge(String[] left, String[] right) {
		String[] _tmp = new String[left.length + right.length];
		System.arraycopy(left, 0, _tmp, 0, left.length);
		System.arraycopy(right, 0, _tmp, left.length, right.length);
		return _tmp;
	}
}

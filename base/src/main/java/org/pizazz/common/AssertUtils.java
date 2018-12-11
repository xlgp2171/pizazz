package org.pizazz.common;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;

/**
 * 断言工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class AssertUtils {

	public static void assertNotNull(String method, Object... argument) throws BaseException {
		if (method == null || ArrayUtils.isEmpty(argument)) {
			return;
		}
		for (int _i = 0; _i < argument.length; _i++) {
			if (argument[_i] == null) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, _i + 1);
				throw new BaseException(BasicCodeEnum.MSG_0001, _msg);
			}
		}
	}

	public static void assertLimit(String method, int index, String target, int length) throws BaseException {
		if (target == null || target.length() != length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void fail(String message) throws BaseException {
		throw new BaseException(message == null ? StringUtils.EMPTY : message);
	}

	public static void assertTrue(String message, boolean target) throws BaseException {
		if (!target) {
			fail(message);
		}
	}

	public static void assertFalse(String message, boolean target) throws BaseException {
		if (target) {
			fail(message);
		}
	}

	// public static void assertNotNull(String message, Object target)
	// throws BaseException {
	// assertFalse(message, target == null);
	// }

	public static void assertNotEmpty(String message, String target) throws BaseException {
		assertFalse(message, StringUtils.isTrimEmpty(target));
	}

	public static void assertNotEmpty(String message, byte[] target) throws BaseException {
		assertFalse(message, ArrayUtils.isEmpty(target));
	}

	public static void assertLimit(String message, int target, Integer min, Integer max) throws BaseException {
		boolean _result = true;

		if (min != null) {
			_result = target >= min;
		}
		if (!_result) {
			_result = false;
		} else if (max != null) {
			_result = target <= max;
		}
		assertTrue(message, _result);
	}

	public static void assertLimit(String message, double target, Double min, Double max) throws BaseException {
		boolean _result = true;

		if (min != null) {
			_result = target >= min;
		}
		if (!_result) {
			_result = false;
		} else if (max != null) {
			_result = target <= max;
		}
		assertTrue(message, _result);
	}

	public static void assertLength(String message, String target, Integer min, Integer max) throws BaseException {
		assertLimit(message, target == null ? 0 : target.length(), min, max);
	}

	public static void assertLength(String message, Object[] target, Integer min, Integer max) throws BaseException {
		assertTrue(message, ArrayUtils.validate(target, min, max));
	}

	public static void assertLength(String message, byte[] target, Integer min, Integer max) throws BaseException {
		assertTrue(message, ArrayUtils.validate(target, min, max));
	}
}

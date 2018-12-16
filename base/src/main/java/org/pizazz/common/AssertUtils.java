package org.pizazz.common;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
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

	public static void assertLength(String method, int index, String target, int length) throws BaseException {
		if (target == null || target.length() != length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertLength(String method, int index, byte[] target, int length) throws BaseException {
		if (target == null || target.length != length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertLimit(String method, int index, int target, Integer min, Integer max) throws BaseException {
		boolean _result = true;
		String _tmp = "";

		if (min != null) {
			if(!(_result = target >= min)) {
				_tmp = ">=" + min;
			}
		}
		if (_result && max != null) {
			if(!(_result = target <= max)) {
				_tmp = "<=" + max;
			}
		}
		if (!_result) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, _tmp);
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
}

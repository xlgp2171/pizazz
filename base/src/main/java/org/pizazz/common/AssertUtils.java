package org.pizazz.common;

import org.pizazz.exception.AssertException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 断言工具
 * 
 * @author xlgp2171
 * @version 1.1.190211
 */
public class AssertUtils {

	public static void assertNotNull(String method, Object... argument) throws AssertException {
		if (ArrayUtils.isEmpty(argument)) {
			return;
		}
		for (int _i = 0; _i < argument.length; _i++) {
			if (argument[_i] == null) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, _i + 1);
				throw new AssertException(BasicCodeEnum.MSG_0001, _msg);
			}
		}
	}

	public static void assertTrue(String method, boolean target) throws AssertException {
		if (!target) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.BOOLEAN", method);
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertEquals(String method, Object[] left, Object[] right) throws AssertException {
		assertNotNull("assertEquals", left, right);

		if (left.length != right.length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "length");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		for (int _i = 0; _i < left.length; _i++) {
			if (!left[_i].equals(right[_i])) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
				throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
			}
		}
	}

	public static void assertEquals(String method, byte[] left, byte[] right) throws AssertException {
		assertNotNull("assertEquals", left, right);

		if (left.length != right.length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "length");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		for (int _i = 0; _i < left.length; _i++) {
			if (left[_i] != right[_i]) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
				throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
			}
		}
	}

	public static void assertEquals(String method, String left, String right) throws AssertException {
		assertNotNull("assertEquals", left, right);

		if (!left.equals(right)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertLength(String method, int index, String target, int length) throws AssertException {
		if (target == null || target.length() != length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertLength(String method, int index, byte[] target, int length) throws AssertException {
		if (target == null || target.length != length) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
	}

	public static void assertLimit(String method, int index, int target, Integer min, Integer max)
			throws AssertException {
		boolean _result = true;
		String _tmp = "";

		if (min != null) {
			if (!(_result = target >= min)) {
				_tmp = ">=" + min;
			}
		}
		if (_result && max != null) {
			if (!(_result = target <= max)) {
				_tmp = "<=" + max;
			}
		}
		if (!_result) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, _tmp);
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
	}
}

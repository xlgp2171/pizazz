package org.pizazz.common;

import java.nio.ByteBuffer;

import org.pizazz.exception.AssertException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 对象工具
 * 
 * @author xlgp2171
 * @version 1.0.190709
 */
public class ObjectUtils {

	public static long getObjectsLength(Object... data) throws AssertException {
		int _length = 0;
		data = ArrayUtils.nullToEmpty(data);

		if (data.length > 0) {
			for (int _i = 0; _i < data.length; _i++) {
				_length += getObjectLength(data[_i]);
			}
		}
		return _length;
	}

	public static long getObjectLength(Object target) throws AssertException {
		long _length;

		if (target == null) {
			_length = NumberUtils.ZERO.longValue();
		} else if (target instanceof ByteBuffer) {
			_length = ((ByteBuffer) target).limit();
		} else if (target instanceof Integer) {
			_length = Integer.BYTES;
		} else if (target instanceof Byte) {
			_length = Byte.BYTES;
		} else if (target instanceof Short) {
			_length = Short.BYTES;
		} else if (target instanceof byte[]) {
			_length = ((byte[]) target).length;
		} else if (target instanceof Character) {
			_length = Character.BYTES;
		} else if (target instanceof Double) {
			_length = Double.BYTES;
		} else if (target instanceof Float) {
			_length = Float.BYTES;
		} else if (target instanceof Long) {
			_length = Long.BYTES;
		} else {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.SUPPORT", "getObjectLength",
					target.getClass().getName());
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		return _length;
	}
}

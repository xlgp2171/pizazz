package org.pizazz2.common;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import org.pizazz2.ISerializable;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 对象工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ObjectUtils {

	public static long getObjectsLength(Object... data) throws ValidateException {
		long length = 0;
		data = ArrayUtils.nullToEmpty(data);

		if (data.length > 0) {
			for (Object item : data) {
				length += ObjectUtils.getObjectLength(item);
			}
		}
		return length;
	}

	public static long getObjectLength(Object target) throws ValidateException {
		long length;

		if (target == null) {
			length = NumberUtils.ZERO.longValue();
		} else if (target instanceof ByteBuffer) {
			length = ((ByteBuffer) target).limit();
		} else if (target instanceof Integer) {
			length = Integer.BYTES;
		} else if (target instanceof Byte) {
			length = Byte.BYTES;
		} else if (target instanceof Short) {
			length = Short.BYTES;
		} else if (target instanceof byte[]) {
			length = ((byte[]) target).length;
		} else if (target instanceof Character) {
			length = Character.BYTES;
		} else if (target instanceof Double) {
			length = Double.BYTES;
		} else if (target instanceof Float) {
			length = Float.BYTES;
		} else if (target instanceof Long) {
			length = Long.BYTES;
		} else if (target instanceof Boolean) {
			length = Byte.BYTES;
		} else if (target instanceof ISerializable) {
			length = ((ISerializable) target).getLength();
		}else {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.SUPPORT", "getObjectLength",
					target.getClass().getName());
			throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
		}
		return length;
	}

	public static boolean isNull(WeakReference<?> target) {
		return target == null || target.get() == null;
	}

	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}
}

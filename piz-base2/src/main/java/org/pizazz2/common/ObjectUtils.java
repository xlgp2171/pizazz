package org.pizazz2.common;

import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.pizazz2.ISerializable;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 对象工具
 * 
 * @author xlgp2171
 * @version 2.2.230323
 */
public class ObjectUtils {

	@SuppressWarnings("unchecked")
	public static <T>T convertPrimitive(Object target, Class<T> clazz) throws IllegalException {
		Object result = null;
		String tmp = StringUtils.of(target);
		try {
			// 包装类型和基本类型通用
			if (clazz == Integer.class) {
				result = Integer.parseInt(tmp);
			} else if (clazz == Byte.class) {
				result = Byte.parseByte(tmp);
			} else if (clazz == Short.class) {
				result = Short.parseShort(tmp);
			} else if (clazz == Double.class) {
				result = Double.parseDouble(tmp);
			} else if (clazz == Float.class) {
				result = Float.parseFloat(tmp);
			} else if (clazz == Long.class) {
				result = Long.parseLong(tmp);
			}
		} catch(NumberFormatException e) {
			throw new IllegalException(BasicCodeEnum.MSG_0005, e.getMessage(), e);
		}
		if (result == null) {
			if (clazz == Boolean.class) {
				result = BooleanUtils.toBoolean(tmp);
			} else if (clazz == String.class) {
				result = tmp;
			} else {
				result = ClassUtils.cast(target, clazz);
			}
		}
		return (T) result;
	}

	public static long getObjectsLength(Object... data) throws IllegalException {
		long length = 0;
		data = ArrayUtils.nullToEmpty(data);

		if (data.length > 0) {
			for (Object item : data) {
				length += ObjectUtils.getObjectLength(item);
			}
		}
		return length;
	}

	public static long getObjectLength(Object target) throws IllegalException {
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
			throw new IllegalException(BasicCodeEnum.MSG_0005, msg);
		}
		return length;
	}

	public static boolean isNotNull(Object target) {
		return !ObjectUtils.isNull(target);
	}

	public static boolean isNull(Object target) {
		boolean tmp = Objects.isNull(target);

		if (!tmp) {
			return target instanceof Reference<?> && ((Reference<?>) target).get() == null;
		}
		return true;
	}

	public static boolean isArray(Object obj) {
		return obj != null && obj.getClass().isArray();
	}
}

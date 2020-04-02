package org.pizazz.common;

import java.nio.ByteBuffer;

import org.pizazz.exception.AssertException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 字节工具
 * 
 * @author xlgp2171
 * @version 1.2.190709
 */
public class BytesUtils {
	public static final byte[] EMPTY = new byte[0];
	
	public static byte[] toBytes(int target) {
		return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(target).array();
	}

	public static int toInt(byte[] target) throws AssertException {
		if (ArrayUtils.isEmpty(target)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "toInt", 1);
			throw new AssertException(BasicCodeEnum.MSG_0001, _msg);
		}
		return ByteBuffer.wrap(target).getInt();
	}

	public static byte[] toBytes(long target) {
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(target).array();
	}

	public static long toLong(byte[] target) throws AssertException {
		if (ArrayUtils.isEmpty(target)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "toLong", 1);
			throw new AssertException(BasicCodeEnum.MSG_0001, _msg);
		}
		return ByteBuffer.wrap(target).getLong();
	}

	static void addObject(Object target, ByteBuffer buffer) {
		if (target instanceof ByteBuffer) {
			buffer.put((ByteBuffer) target);
		} else if (target instanceof Integer) {
			buffer.putInt((int) target);
		} else if (target instanceof Byte) {
			buffer.put((byte) target);
		} else if (target instanceof Short) {
			buffer.putShort((short) target);
		} else if (target instanceof byte[]) {
			buffer.put((byte[]) target);
		} else if (target instanceof Character) {
			buffer.putChar((char) target);
		} else if (target instanceof Double) {
			buffer.putDouble((double) target);
		} else if (target instanceof Float) {
			buffer.putFloat((float) target);
		} else if (target instanceof Long) {
			buffer.putLong((long) target);
		}
	}

	public static byte[] buffer(Object... data) throws AssertException {
		int _length = 0;
		data = ArrayUtils.nullToEmpty(data);

		if (data.length == 0) {
			return BytesUtils.EMPTY;
		}
		for (int _i = 0; _i < data.length; _i++) {
			if (data[_i] == null) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "buffer", _i);
				throw new AssertException(BasicCodeEnum.MSG_0001, _msg);
			}
			_length += ObjectUtils.getObjectLength(data[_i]);
		}
		ByteBuffer _tmp = ByteBuffer.allocate(_length);

		for (Object _item : data) {
			addObject(_item, _tmp);
		}
		return _tmp.array();
	}
}

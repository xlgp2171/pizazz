package org.pizazz.common;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.pizazz.common.ArrayUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 字节工具
 * 
 * @author xlgp2171
 * @version 1.0.181224
 */
public class BytesUtils {
	public static byte[] toBytes(int target) {
		return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(target).array();
	}

	public static int toInt(byte[] target) throws BaseException {
		if (ArrayUtils.isEmpty(target)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "toInt", 1);
			throw new BaseException(BasicCodeEnum.MSG_0001, _msg);
		}
		return ByteBuffer.wrap(target).getInt();
	}

	public static byte[] toBytes(long target) {
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(target).array();
	}

	public static long toLong(byte[] target) throws BaseException {
		if (ArrayUtils.isEmpty(target)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "toLong", 1);
			throw new BaseException(BasicCodeEnum.MSG_0001, _msg);
		}
		return ByteBuffer.wrap(target).getLong();
	}

	public static byte[] toByteArray(String target, Charset charset) {
		return target.getBytes(charset);
	}

	public static String toString(byte[] target, Charset charset) {
		return new String(target, charset);
	}

	public static byte[] buffer(int length, Object... data) {
		ByteBuffer _tmp = ByteBuffer.allocate(length > 0 ? length : 0);

		for (Object _item : data) {
			if (_item instanceof ByteBuffer) {
				_tmp.put((ByteBuffer) _item);
			} else if (_item instanceof Integer) {
				_tmp.putInt((int) _item);
			} else if (_item instanceof Byte) {
				_tmp.put((byte) _item);
			} else if (_item instanceof Short) {
				_tmp.putShort((short) _item);
			} else if (_item instanceof byte[]) {
				_tmp.put((byte[]) _item);
			} else if (_item instanceof Short) {
				_tmp.putShort((short) _item);
			} else if (_item instanceof Character) {
				_tmp.putChar((char) _item);
			} else if (_item instanceof Double) {
				_tmp.putDouble((double) _item);
			} else if (_item instanceof Float) {
				_tmp.putFloat((float) _item);
			} else if (_item instanceof Long) {
				_tmp.putLong((long) _item);
			}
		}
		return _tmp.array();
	}
}

package org.pizazz.common;

import java.nio.ByteBuffer;

import org.pizazz.common.ArrayUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 字节工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
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
}

package org.pizazz2.common;

import java.nio.ByteBuffer;

import org.pizazz2.ISerializable;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 字节工具
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class BytesUtils {

    public static byte[] toBytes(long target) {
        return ByteBuffer.allocate(Long.BYTES).putLong(target).array();
    }

    public static byte[] toBytes(int target) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(target).array();
    }

    public static byte[] toBytes(short target) {
        return ByteBuffer.allocate(Short.BYTES).putShort(target).array();
    }

    public static byte[] toBytes(double target) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(target).array();
    }

    public static byte[] toBytes(float target) {
        return ByteBuffer.allocate(Float.BYTES).putFloat(target).array();
    }

    public static byte[] toBytes(char target) {
        return ByteBuffer.allocate(Character.BYTES).putChar(target).array();
    }

    public static byte[] toBytes(boolean target) {
        return ByteBuffer.allocate(Byte.BYTES).put(target ? NumberUtils.ONE.byteValue() : NumberUtils.ZERO.byteValue()).array();
    }

    public static long toLong(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toLong", target);
        return ByteBuffer.wrap(target).getLong();
    }

    public static int toInt(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toInt", target);
        return ByteBuffer.wrap(target).getInt();
    }

    public static short toShort(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toShort", target);
        return ByteBuffer.wrap(target).getShort();
    }

    public static double toDouble(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toDouble", target);
        return ByteBuffer.wrap(target).getDouble();
    }

    public static float toFloat(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toFloat", target);
        return ByteBuffer.wrap(target).getLong();
    }

    public static char toChar(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toChar", target);
        return ByteBuffer.wrap(target).getChar();
    }

    public static boolean toBoolean(byte[] target) throws ValidateException {
        ValidateUtils.notEmpty("toBoolean", target);
        return ByteBuffer.wrap(target).get() == NumberUtils.ONE.byteValue();
    }

    static void addObject(Object target, ByteBuffer buffer) throws ValidateException {
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
        } else if (target instanceof Boolean) {
            buffer.put(((boolean) target) ? NumberUtils.ONE.byteValue() : NumberUtils.ZERO.byteValue());
        } else if (target instanceof ISerializable) {
            buffer.put(((ISerializable) target).serialize());
        } else {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.SUPPORT", "addObject", target.getClass().getName());
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static byte[] buffer(Object... data) throws ValidateException {
        if (ArrayUtils.isEmpty(data)) {
            return ArrayUtils.EMPTY_BYTE;
        }
        int length = 0;
        data = ArrayUtils.nullToEmpty(data);

        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "buffer", i);
                throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
            }
            length += ObjectUtils.getObjectLength(data[i]);
        }
        ByteBuffer tmp = ByteBuffer.allocate(length);

        for (Object item : data) {
            BytesUtils.addObject(item, tmp);
        }
        return tmp.array();
    }
}

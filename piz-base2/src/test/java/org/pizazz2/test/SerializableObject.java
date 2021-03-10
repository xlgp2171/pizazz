package org.pizazz2.test;

import org.pizazz2.ISerializable;
import org.pizazz2.common.BytesUtils;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.exception.ValidateException;

import java.nio.ByteBuffer;

public class SerializableObject implements ISerializable {
    private static final long serialVersionUID = -1L;

    private boolean bool;
    private double num;
    private String text;

    public SerializableObject() {
    }

    public SerializableObject(boolean bool, double num, String text) {
        this.bool = bool;
        this.num = num;
        this.text = text;
    }

    @Override
    public byte[] serialize() throws ValidateException {
        return BytesUtils.buffer(bool, num, text.getBytes(getCharset()));
    }

    @Override
    public void deserialize(byte[] data) throws ValidateException {
        ByteBuffer tmp = ByteBuffer.wrap(data);
        bool = tmp.get() == NumberUtils.ONE.byteValue();
        num = tmp.getDouble();
        byte[] b = new byte[data.length - Byte.BYTES - Double.BYTES];
        tmp.get(b);
        text = new String(b, getCharset());
    }

    @Override
    public long getLength() {
        return Byte.BYTES + Double.BYTES + text.getBytes(getCharset()).length;
    }

    public boolean getBool() {
        return bool;
    }

    public double getNum() {
        return num;
    }

    public String getText() {
        return text;
    }
}

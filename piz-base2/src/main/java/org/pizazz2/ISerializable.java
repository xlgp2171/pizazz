package org.pizazz2;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.pizazz2.exception.ValidateException;

/**
 * 序列化接口
 *
 * @author xlgp2171
 * @version 3.0.250102
 */
public interface ISerializable<T extends Serializable> extends Serializable {
    /**
     * 对象序列化
     * @return 数据二进制流
     * @throws ValidateException 序列化异常
     */
    byte[] serialize() throws ValidateException;

    /**
     * 对象反序列化
     * @param data 数据二进制流
     * @throws ValidateException 反序列化异常
     */
    T deserialize(byte[] data) throws ValidateException;

    /**
     * 获取编码格式
     * @return 编码格式
     */
    default Charset getCharset() {
        return StandardCharsets.UTF_8;
    }
    /**
     * 获取对象字节长度
     * @return 对象字节长度
     */
    default long getLength() {
        return 0;
    }
}

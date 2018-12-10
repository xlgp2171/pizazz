package org.pizazz;

import java.io.Serializable;
import java.nio.charset.Charset;

import org.pizazz.exception.BaseException;

/**
 * 序列化接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface ISerializable extends Serializable {

	public byte[] serialize() throws BaseException;
	public void deserialize(byte[] data) throws BaseException;
	public default void setCharset(Charset charset) {};
}

package org.pizazz;

import org.pizazz.exception.BaseException;

/**
 * 消息输出接口<br>
 * 提供自定义消息输出接口，包括消息输出、异常输出等
 * 
 * @param <T> 根据输出内容注册泛型
 * 
 * @author xlgp2171
 * @version 1.0.180601
 */
public interface IMessageOutput<T> extends ICloseable {

	public default boolean isEnable() {
		return false;
	};

	public void write(T message);

	public default void throwException(Exception e) {
	};

	@Override
	public default void close(int timeout) throws BaseException {
	}
}

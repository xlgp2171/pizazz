package org.pizazz;

import java.time.Duration;

import org.pizazz.exception.BaseException;

/**
 * 关闭接口<br>
 * 提供自动关闭方法
 * 
 * @author xlgp2171
 * @version 1.1.181219
 * 
 * @see AutoCloseable
 */
public interface ICloseable extends AutoCloseable {
	@Override
	public default void close() throws BaseException {
		destroy(Duration.ZERO);
	}

	public void destroy(Duration timeout) throws BaseException;
}

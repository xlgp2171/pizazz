package org.pizazz;

import org.pizazz.exception.BaseException;

/**
 * 关闭接口<br>
 * 提供自动关闭方法
 * 
 * @author xlgp2171
 * @version 1.0.181210
 * 
 * @see AutoCloseable
 */
public interface ICloseable extends AutoCloseable {
	@Override
	public default void close() throws BaseException {
		close(0);
	}

	public void close(int timeout) throws BaseException;
}

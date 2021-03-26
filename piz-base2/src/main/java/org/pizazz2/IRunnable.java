package org.pizazz2;

import org.pizazz2.exception.BaseException;

/**
 * 类运行接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IRunnable extends ICloseable, Runnable {
	/**
	 * 继承run方法
	 */
	@Override
	default void run() {
		try {
			activate();
		} catch (BaseException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 激活
	 * @throws BaseException 初始化异常
	 */
	void activate() throws BaseException;
}

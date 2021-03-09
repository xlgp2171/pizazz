package org.pizazz2;

import org.pizazz2.exception.AbstractException;

/**
 * 类运行接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IRunnable extends IObject, ICloseable, Runnable {
	/**
	 * 是否启用
	 * @throws AbstractException 初始化异常
	 */
	default void activate() throws AbstractException {
	}
}

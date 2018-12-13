package org.pizazz;

import org.pizazz.exception.BaseException;

/**
 * 类运行接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IRunnable extends IObject, ICloseable, Runnable {

	public default void enable() throws BaseException {
	}
}

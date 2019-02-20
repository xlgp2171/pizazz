package org.pizazz;

import org.pizazz.exception.AbstractException;

/**
 * 类运行接口
 * 
 * @author xlgp2171
 * @version 1.1.190220
 * 
 * @see IObject
 * @see ICloseable
 */
public interface IRunnable extends IObject, ICloseable, Runnable {

	public default void enable() throws AbstractException {
	}
}

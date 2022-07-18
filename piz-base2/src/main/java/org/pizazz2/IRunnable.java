package org.pizazz2;

import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.BaseRuntimeException;
import org.pizazz2.message.BasicCodeEnum;

/**
 * 类运行接口
 * 
 * @author xlgp2171
 * @version 2.1.220715
 */
public interface IRunnable extends ICloseable, Runnable {
	/**
	 * 继承run方法
	 */
	@Override
	default void run() {
		try {
			activate();
		} catch (Exception e) {
			if (throwable()) {
				throw new BaseRuntimeException(BasicCodeEnum.MSG_0030, e);
			} else {
				throwException(e);
			}
		} finally {
			complete();
		}
	}
	/**
	 * 激活处理
	 * @throws BaseException 初始化异常
	 */
	void activate() throws BaseException;

	/**
	 * 处理完成步骤（无论是否异常）
	 */
	default void complete() {}

	/**
	 * 当{@link IRunnable#throwable}为false时，用于处理异常
	 * @param e 异常
	 */
	default void throwException(Exception e) {}

	/**
	 * 是否抛出异常
	 * @return 是否直接抛出异常
	 */
	default boolean throwable() {
		return false;
	}
}

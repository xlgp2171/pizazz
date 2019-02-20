package org.pizazz;

import java.time.Duration;

/**
 * 关闭接口<br>
 * 提供自动关闭方法
 * 
 * @author xlgp2171
 * @version 1.2.190220
 */
public interface ICloseable extends AutoCloseable {
	@Override
	public default void close() {
		destroy(Duration.ZERO);
	}

	public void destroy(Duration timeout);
}

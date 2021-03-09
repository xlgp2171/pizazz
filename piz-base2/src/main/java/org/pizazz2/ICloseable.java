package org.pizazz2;

import java.time.Duration;

/**
 * 关闭接口
 * <li/>提供自动关闭方法，可使用try(){}方法自动关闭
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface ICloseable extends AutoCloseable {
    /**
     * 关闭<br>
     * 默认实现为：destroy(Duration.ZERO)
     */
    @Override
    default void close() {
        destroy(Duration.ZERO);
    }

    /**
     * 销毁<br>
     *
     * @param timeout 作为销毁的超时设置
     */
    void destroy(Duration timeout);
}

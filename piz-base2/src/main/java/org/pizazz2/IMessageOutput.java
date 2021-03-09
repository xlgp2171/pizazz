package org.pizazz2;

import org.pizazz2.exception.AbstractException;

import java.time.Duration;

/**
 * 消息输出接口
 * <li/>提供自定义消息输出接口，包括消息输出、异常输出等
 * <li/>支持lambda表达式，实现{@link #write}方法
 *
 * @param <T> 根据输出内容注册泛型
 * @author xlgp2171
 * @version 2.0.210201
 * @see AbstractException
 */
@FunctionalInterface
public interface IMessageOutput<T> extends ICloseable {
    static final IMessageOutput<String> EMPTY_STRING = message -> message = null;
    static final IMessageOutput<String> EMPTY_STRING_ENABLED = new IMessageOutput<String>() {
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void write(String message) {
        }
    };

    /**
     * 自定义启用
     * <li/>默认实现为不启用
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return false;
    }

    /**
     * 输出消息内容
     *
     * @param message 消息内容
     */
    void write(T message);

    /**
     * 输出消息异常
     * <li/>默认实现为空方法
     * @param exception 消息异常
     */
    default void throwException(Exception exception) {
    }

    /**
     * 销毁
     * <li/>默认实现为空方法
     *
     * @param timeout 作为销毁的超时设置
     */
    @Override
    default void destroy(Duration timeout) {
    }
}

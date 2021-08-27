package org.pizazz2.tool.ref;

import java.util.Collection;

/**
 * 流式处理监听
 *
 * @author xlgp2171
 * @version 2.0.210827
 *
 * @param <T> 处理类
 */
@FunctionalInterface
public interface IDataflowListener<T> {
    /**
     * 前置处理
     *
     * @param executionId 运行ID
     * @param dataList 数据组
     */
    default void before(long executionId, Collection<T> dataList) {
    }

    /**
     * 后置处理
     *
     * @param executionId 运行ID
     * @param dataList 数据组
     */
    void after(long executionId, Collection<T> dataList);

    /**
     * 异常处理
     *
     * @param executionId 运行ID
     * @param dataList 数据组
     * @param e 处理异常
     */
    default void exception(long executionId, Collection<T> dataList, Exception e) {
    }

    /**
     * 代理监听接口
     *
     * @param <T> 处理类
     */
    class ProxyListener<T> implements IDataflowListener<T> {
        private final IDataflowListener<T> listener;

        public ProxyListener(IDataflowListener<T> listener) {
            this.listener = listener == null ? (executionId, dataList) -> {} : listener;
        }

        @Override
        public void before(long executionId, Collection<T> dataList) {
            listener.before(executionId, dataList);
        }

        @Override
        public void after(long executionId, Collection<T> dataList) {
            listener.after(executionId, dataList);
        }

        @Override
        public void exception(long executionId, Collection<T> dataList, Exception e) {
            listener.exception(executionId, dataList, e);
        }
    }
}

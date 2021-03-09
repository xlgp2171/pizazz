package org.pizazz2;

import org.pizazz2.exception.AbstractException;

/**
 * 动态插件接口
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IPlugin extends IObject, ICloseable {

    /**
     * 初始化
     *
     * @param config 初始化配置
     * @throws AbstractException 抛出初始化异常
     */
    void initialize(IObject config) throws AbstractException;
}

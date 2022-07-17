package org.pizazz2;

import org.pizazz2.exception.BaseException;

/**
 * 动态插件接口
 *
 * @author xlgp2171
 * @version 2.1.211201
 */
public interface IPlugin<C extends IObject> extends IObject, ICloseable {

    /**
     * 初始化
     *
     * @param config 初始化配置
     * @throws BaseException 抛出初始化异常
     */
    void initialize(C config) throws BaseException;
}

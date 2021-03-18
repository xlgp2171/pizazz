package org.pizazz2;

import org.pizazz2.common.SystemUtils;

/**
 * 对象接口
 * <li/>需要对象处理时使用
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IObject extends Cloneable {

    /**
     * 获取对象唯一ID
     *
     * @return 对象唯一ID
     */
    default String getId() {
        return SystemUtils.createId(this);
    }

    /**
     * 对象是否为空
     * @return True/False:是/否
     */
    boolean isEmpty();

    /**
     * 更新对象
     *
     * @param key 标识
     * @param target 目标对象
     */
    default void set(String key, Object target) {
    }

    /**
     * 重置对象
     */
    default void reset() {
    }

    /**
     * 复制对象
     * @return 复制对象
     */
    default IObject copy() {
        return this;
    }

    /**
     * 获取对应字符串
     * @param key 标识
     * @param defValue 默认值
     * @return 标识对应值
     */
    default Object get(String key, Object defValue) {
        return defValue;
    }
}

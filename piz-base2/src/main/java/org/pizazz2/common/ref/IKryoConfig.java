package org.pizazz2.common.ref;

import com.esotericsoftware.kryo.Kryo;

/**
 * KRYO配置接口
 *
 * @author xlgp2171
 * @version 3.0.250102
 */
public interface IKryoConfig {
    IKryoConfig EMPTY = new IKryoConfig() {
    };

    /**
     * kryo设置
     *
     * @param kryo kryo对象
     */
    default void set(Kryo kryo) {
    }

    /**
     * 获取缓冲大小
     *
     * @return 缓冲大小
     */
    default int getBufferSize() {
        return 4096;
    }
}

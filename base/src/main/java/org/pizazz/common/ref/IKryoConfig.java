package org.pizazz.common.ref;

import com.esotericsoftware.kryo.Kryo;

/**
 * KRYO配置接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IKryoConfig {
	public default void set(Kryo kryo) {
	}

	public default int getBufferSize() {
		return 4096;
	}
}

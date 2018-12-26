package org.pizazz.redis;

import org.pizazz.IPlugin;

public interface IRedisInstance extends IPlugin {

	public IRedisProcessor getProcessor();
}

package org.pizazz.redis;

import org.pizazz.IPlugin;

public interface IRedisAdapter extends IPlugin {

	public IRedisProcessor getProcessor();
}

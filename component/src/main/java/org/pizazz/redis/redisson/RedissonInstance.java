package org.pizazz.redis.redisson;

import org.redisson.Redisson;
import org.redisson.RedissonMap;
import org.redisson.api.RMap;
import org.redisson.config.Config;

class RedissonInstance extends Redisson {

	RedissonInstance(Config config) {
		super(config);
	}

	RMap<String, String> getSSMap(String name) {
        return new RedissonMap<String, String>(connectionManager.getCommandExecutor(), name, this, null);
    }
}

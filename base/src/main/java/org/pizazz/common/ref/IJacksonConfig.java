package org.pizazz.common.ref;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson配置接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IJacksonConfig {

	public default JsonFactory factory() {
		return null;
	}

	public default void set(ObjectMapper mapper) {
	}
}

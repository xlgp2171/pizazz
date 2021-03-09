package org.pizazz2.common.ref;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson配置接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IJacksonConfig {

	static final IJacksonConfig EMPTY = new IJacksonConfig() {
	};
	/**
	 * 构建jackson工厂
	 * @return jackson工厂
	 */
	default JsonFactory factory() {
		return null;
	}

	/**
	 * 设置mapper
	 * @param mapper jackson的Mapper
	 */
	default void set(ObjectMapper mapper) {
	}
}

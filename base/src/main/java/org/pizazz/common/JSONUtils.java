package org.pizazz.common;

import java.io.IOException;

import org.pizazz.common.ref.IJacksonConfig;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 对象JSON处理<br>
 * 使用jackson组件
 * 
 * @author xlgp2171
 * @version 1.1.190219
 */
public class JSONUtils {

	public static String toJSON(Object target, IJacksonConfig config) throws UtilityException {
		if (target == null) {
			return "{}";
		} else if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				config.factory());
		config.set(_mapper);
		try {
			return _mapper.writeValueAsString(target);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static String toJSON(Object target) throws UtilityException {
		return toJSON(target, null);
	}

	public static <T> T fromJSON(String target, Class<T> type, IJacksonConfig config)
			throws AssertException, UtilityException {
		AssertUtils.assertNotNull("fromJSON", target, type);

		if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				config.factory());
		config.set(_mapper);
		try {
			return _mapper.readValue(target, type);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static <T> T fromJSON(String target, Class<T> type) throws AssertException, UtilityException {
		return fromJSON(target, type, null);
	}
}

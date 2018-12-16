package org.pizazz.common;

import java.io.IOException;
import java.io.InputStream;

import org.pizazz.Constant;
import org.pizazz.common.ref.IJacksonConfig;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 对象YAML处理<br>
 * 使用jackson组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class YAMLUtils {

	public static <T> T fromYAML(InputStream target, Class<T> type, IJacksonConfig config) throws BaseException {
		AssertUtils.assertNotNull("fromYAML", target, type);

		if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
		config.set(_mapper);
		try {
			return _mapper.readValue(target, type);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static TupleObject fromYAML(InputStream target) throws BaseException {
		AssertUtils.assertNotNull("fromYAML", target);
		try {
			return fromYAML(target, TupleObject.class, null);
		} finally {
			IOUtils.close(target);
		}
	}

	public static TupleObject fromYAML(String resource) throws BaseException {
		try (InputStream _tmp = IOUtils.getResourceAsStream(resource, Constant.class, null)) {
			return fromYAML(_tmp, TupleObject.class, null);
		} catch (IOException e) {
			return TupleObjectHelper.emptyObject();
		}
	}
}

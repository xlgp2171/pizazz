package org.pizazz.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.pizazz.Constant;
import org.pizazz.common.ref.IJacksonConfig;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 对象YAML处理<br>
 * 使用jackson组件
 * 
 * @author xlgp2171
 * @version 1.2.190219
 */
public class YAMLUtils {

	public static <T> T fromYAML(InputStream target, Class<T> type, IJacksonConfig config)
			throws AssertException, UtilityException {
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
			throw new UtilityException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static TupleObject fromYAML(InputStream target) throws AssertException, UtilityException {
		AssertUtils.assertNotNull("fromYAML", target);
		try {
			return fromYAML(target, TupleObject.class, null);
		} finally {
			IOUtils.close(target);
		}
	}

	public static TupleObject fromYAML(String resource) throws AssertException, UtilityException {
		try (InputStream _tmp = IOUtils.getResourceAsStream(resource, Constant.class, null)) {
			return fromYAML(_tmp, TupleObject.class, null);
		} catch (IOException e) {
			return TupleObjectHelper.emptyObject();
		}
	}

	public static <T> void toYAML(OutputStream target, T data, IJacksonConfig config)
			throws AssertException, UtilityException {
		AssertUtils.assertNotNull("toYAML", target, data);

		if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
		config.set(_mapper);
		try {
			_mapper.writeValue(target, data);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static void toYAML(OutputStream target, TupleObject data) throws AssertException, UtilityException {
		AssertUtils.assertNotNull("toYAML", target, data);
		try {
			toYAML(target, data, null);
		} finally {
			IOUtils.close(target);
		}
	}

	public static void toYAML(Path path, TupleObject data) throws AssertException, UtilityException {
		toYAML(PathUtils.getOutputStream(path), data);
	}
}

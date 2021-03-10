package org.pizazz2.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.pizazz2.Constant;
import org.pizazz2.common.ref.IJacksonConfig;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 对象YAML处理<br>
 * 使用jackson组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class YAMLUtils {

	public static <T> T fromYAML(InputStream target, Class<T> type, IJacksonConfig config)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", target, type);

		if (config == null) {
			config = IJacksonConfig.EMPTY;
		}
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
		config.set(mapper);
		try {
			return mapper.readValue(target, type);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static TupleObject fromYAML(InputStream target) throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", target);
		try {
			return YAMLUtils.fromYAML(target, TupleObject.class, null);
		} finally {
			SystemUtils.close(target);
		}
	}

	/**
	 * 从资源路径读取YAML文件转换为通用对象
	 * @param resource 资源路径
	 * @return 通用对象
	 * @throws ValidateException 参数验证
	 * @throws UtilityException YAML转换异常
	 */
	public static TupleObject fromYAML(String resource) throws ValidateException, UtilityException {
		try (InputStream tmp = IOUtils.getResourceAsStream(resource, Constant.class, null)) {
			return YAMLUtils.fromYAML(tmp, TupleObject.class, IJacksonConfig.EMPTY);
		} catch (IOException e) {
			return TupleObjectHelper.emptyObject();
		}
	}

	public static <T> void toYAML(OutputStream target, T data, IJacksonConfig config)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("toYAML", target, data);

		if (config == null) {
			config = IJacksonConfig.EMPTY;
		}
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper(
				new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
		config.set(mapper);
		try {
			mapper.writeValue(target, data);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static void toYAML(OutputStream target, TupleObject data) throws ValidateException, UtilityException {
		ValidateUtils.notNull("toYAML", target, data);
		try {
			YAMLUtils.toYAML(target, data, IJacksonConfig.EMPTY);
		} finally {
			SystemUtils.close(target);
		}
	}

	/**
	 * 将通用对象写入YAML文件
	 * @param path YAML文件路径
	 * @param data 数据
	 * @throws ValidateException 参数验证
	 * @throws UtilityException 数据转换异常
	 */
	public static void toYAML(Path path, TupleObject data) throws ValidateException, UtilityException {
		YAMLUtils.toYAML(PathUtils.getOutputStream(path), data);
	}
}

package org.pizazz2.common;

import java.io.IOException;

import org.pizazz2.common.ref.IJacksonConfig;
import org.pizazz2.common.ref.IKryoConfig;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 对象序列化工具
 * <li/>序列化采用kryo实现
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class SerializationUtils {
	public static byte[] serialize(Object target, IKryoConfig config) throws ValidateException, UtilityException {
		ValidateUtils.notNull("serialize", target);

		if (config == null) {
			config = IKryoConfig.EMPTY;
		}
		com.esotericsoftware.kryo.Kryo kryo = new com.esotericsoftware.kryo.Kryo();
		config.set(kryo);
		int buffer = config.getBufferSize();

		try (com.esotericsoftware.kryo.io.Output output = new com.esotericsoftware.kryo.io.Output(
				buffer > 0 ? buffer : 4096, Integer.MAX_VALUE)) {
			kryo.writeObject(output, target);
			return output.toBytes();
		} catch (Exception e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.KRYO.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static <T> T deserialize(byte[] target, Class<T> type, IKryoConfig config)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("deserialize", target, type);
		com.esotericsoftware.kryo.Kryo kryo = new com.esotericsoftware.kryo.Kryo();

		if (config != null) {
			config.set(kryo);
		}
		try (com.esotericsoftware.kryo.io.Input input = new com.esotericsoftware.kryo.io.Input(target)) {
			return kryo.readObject(input, type);
		} catch (Exception e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.KRYO.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static byte[] serialize(Object target, IJacksonConfig config) throws ValidateException, UtilityException {
		ValidateUtils.notNull("serialize", target);

		if (config == null) {
			config = IJacksonConfig.EMPTY;
		}
		com.fasterxml.jackson.databind.ObjectMapper mapper;
		mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
		config.set(mapper);
		try {
			return mapper.writeValueAsBytes(target);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static <T> T deserialize(byte[] target, Class<T> type, IJacksonConfig config)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("deserialize", target, type);

		if (config == null) {
			config = IJacksonConfig.EMPTY;
		}
		com.fasterxml.jackson.databind.ObjectMapper mapper;
		mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
		config.set(mapper);
		try {
			return mapper.readValue(target, type);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}
}

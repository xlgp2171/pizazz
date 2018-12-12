package org.pizazz.common;

import java.io.IOException;

import org.pizazz.common.ref.IJacksonConfig;
import org.pizazz.common.ref.IKryoConfig;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;

/**
 * 对象序列化工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class SerializationUtils {
	public static byte[] serialize(Object target, IKryoConfig config) throws BaseException {
		AssertUtils.assertNotNull("serialize", target);

		if (config == null) {
			config = new IKryoConfig() {
			};
		}
		com.esotericsoftware.kryo.Kryo _kryo = new com.esotericsoftware.kryo.Kryo();
		config.set(_kryo);
		int _buffer = config.getBufferSize();

		try (com.esotericsoftware.kryo.io.Output _output = new com.esotericsoftware.kryo.io.Output(
				_buffer > 0 ? _buffer : 4096, Integer.MAX_VALUE)) {
			_kryo.writeObject(_output, target);
			return _output.toBytes();
		} catch (Exception e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.KRYO.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static <T> T deserialize(byte[] target, Class<T> type, IKryoConfig config) throws BaseException {
		AssertUtils.assertNotNull("deserialize", target, type);
		com.esotericsoftware.kryo.Kryo _kryo = new com.esotericsoftware.kryo.Kryo();

		if (config != null) {
			config.set(_kryo);
		}
		com.esotericsoftware.kryo.io.Input _input = new com.esotericsoftware.kryo.io.Input(target);
		try {
			return _kryo.readObject(_input, type);
		} catch (Exception e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.KRYO.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0013, _msg, e);
		} finally {
			_input.close();
		}
	}

	public static byte[] serialize(Object target, IJacksonConfig config) throws BaseException {
		AssertUtils.assertNotNull("serialize", target);

		if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper;
		_mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
		config.set(_mapper);
		try {
			return _mapper.writeValueAsBytes(target);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}

	public static <T> T deserialize(byte[] target, Class<T> type, IJacksonConfig config) throws BaseException {
		AssertUtils.assertNotNull("deserialize", target, type);

		if (config == null) {
			config = new IJacksonConfig() {
			};
		}
		com.fasterxml.jackson.databind.ObjectMapper _mapper;
		_mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
		config.set(_mapper);
		try {
			return _mapper.readValue(target, type);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0013, _msg, e);
		}
	}
}

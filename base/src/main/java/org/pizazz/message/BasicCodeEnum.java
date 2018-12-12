package org.pizazz.message;

import org.pizazz.message.ref.IMessageCode;

/**
 * 消息识别码
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public enum BasicCodeEnum implements IMessageCode {
	/** 预留 */
	MSG_0000("MSG0000#"),
	/** NullPointException */
	MSG_0001("MSG0001#"),
	/** SecurityException */
	MSG_0002("MSG0002#"),
	/** IOException */
	MSG_0003("MSG0003#"),
	/** 类转换异常 */
	MSG_0004("MSG0004#"),
	/** IllegalArgumentException */
	MSG_0005("MSG0005#"),
	/** 目标异常 InvocationTargetException */
	MSG_0006("MSG0006#"),
	/** 反射内容异常 IllegalAccessException */
	MSG_0007("MSG0007#"),
	/** 实例化异常 InstantiationException */
	MSG_0008("MSG0008#"),
	/** ClassNotFoundException */
	MSG_0009("MSG0009#"),
	/** ReflectException */
	MSG_0010("MSG0010#"),
	/** RegistryException */
	MSG_0011("MSG0011#"),
	/** InterruptedException */
	MSG_0012("MSG0012#"),
	/** SerializationException */
	MSG_0013("MSG0013#"),
	/** PluginException */
	MSG_0014("MSG0014#"),
	/** CryptoException */
	MSG_0015("MSG0015#"),
	/** SocketException */
	MSG_0016("MSG0016#"),
	/** DateException */
	MSG_0017("MSG0017#"),
	/** XMLException */
	MSG_0018("MSG0018#");

	private String code;
	private StringBuffer message;

	private BasicCodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getPrefix() {
		return "ERR";
	}

	@Override
	public String getValue() {
		return message == null ? code : code + message.toString();
	}

	@Override
	public IMessageCode append(Object target) {
		if (message == null) {
			synchronized (message) {
				if (message == null) {
					message = new StringBuffer();
				}
			}
		}
		message.append(target);
		return this;
	}
}

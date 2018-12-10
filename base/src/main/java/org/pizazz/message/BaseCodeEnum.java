package org.pizazz.message;

/**
 * 消息识别码
 * 
 * @author xlgp2171
 * @version 1.0.151010
 */
public enum BaseCodeEnum implements IMessageCode {
	/**
	 * 
	 */
	MSG_0000("MSG0000#"),
	/** 目标未找到 NullPointException */
	MSG_0001("MSG0001#"),
	/** 没有权限 SecurityException */
	MSG_0002("MSG0002#"),
	/** 流异常 IOException */
	MSG_0003("MSG0003#"),
	/** 类转换异常 */
	MSG_0004("MSG0004#"),
	/** 参数异常 IllegalArgumentException */
	MSG_0005("MSG0005#"),
	/** 目标异常 InvocationTargetException */
	MSG_0006("MSG0006#"),
	/** 反射内容异常 IllegalAccessException */
	MSG_0007("MSG0007#"),
	/** 实例化异常 InstantiationException */
	MSG_0008("MSG0008#");

	private String code;
	private StringBuffer message;

	private BaseCodeEnum(String code) {
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

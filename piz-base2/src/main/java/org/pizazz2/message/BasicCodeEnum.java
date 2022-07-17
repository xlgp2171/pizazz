package org.pizazz2.message;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 消息识别码
 * 
 * @author xlgp2171
 * @version 2.0.210914
 */
public enum BasicCodeEnum implements IMessageCode {
	/** 预留 */
	MSG_0000("MSG0000#"),
	/** 空指针异常：NullPointException */
	MSG_0001("MSG0001#"),
	/** 安全异常：SecurityException */
	MSG_0002("MSG0002#"),
	/** 输入输出异常：IOException */
	MSG_0003("MSG0003#"),
	/** 类转换异常：ClassCastException */
	MSG_0004("MSG0004#"),
	/** 非法参数异常：IllegalArgumentException */
	MSG_0005("MSG0005#"),
	/** 调用目标异常：InvocationTargetException */
	MSG_0006("MSG0006#"),
	/** 非法使用异常：IllegalAccessException */
	MSG_0007("MSG0007#"),
	/** 实例化异常：InstantiationException */
	MSG_0008("MSG0008#"),
	/** 类无法找到异常：ClassNotFoundException */
	MSG_0009("MSG0009#"),
	/** 反射异常：ReflectException */
	MSG_0010("MSG0010#"),
	/** 注册异常：RegistryException */
	MSG_0011("MSG0011#"),
	/** 中断异常：InterruptedException */
	MSG_0012("MSG0012#"),
	/** 序列化异常：SerializationException */
	MSG_0013("MSG0013#"),
	/** 插件异常：PluginException */
	MSG_0014("MSG0014#"),
	/** 加密解密异常：CryptoException */
	MSG_0015("MSG0015#"),
	/** 套接字异常：SocketException */
	MSG_0016("MSG0016#"),
	/** 日期时间异常：DateException */
	MSG_0017("MSG0017#"),
	/** XML异常：XMLException */
	MSG_0018("MSG0018#"),
	/** 日志异常：LogException */
	MSG_0019("MSG0019#"),
	/** 初始化异常：InitializeException */
	MSG_0020("MSG0020#"),
	/** 数据库异常：DatabaseException */
	MSG_0021("MSG0021#"),
	/** 编译异常：CompileException */
	MSG_0022("MSG0022#"),
	/** 参数无法找到异常: ArgumentNotFoundException */
	MSG_0023("MSG0023#"),
	/** 解析异常: ParseException */
	MSG_0024("MSG0024#"),
	/** 超时异常: TimeoutException */
	MSG_0025("MSG0025#"),
	/** 数据匹配异常: MatchException */
	MSG_0026("MSG0026#"),
	/** 查找目标无法找到异常: TargetNotFoundException */
	MSG_0027("MSG0027#"),
	/** 格式转换异常: FormatException */
	MSG_0028("MSG0028#"),
	/** 第三方组件连接异常: ConnectionException */
	MSG_0029("MSG0029#"),
	/** 方法调用异常: MethodInvokeException */
	MSG_0030("MSG0030#"),
	/** 状态异常: StateException */
	MSG_0031("MSG0031#"),
	;
	

	private final String code;

	BasicCodeEnum(String code) {
		this.code = code;
	}

	@Override
	public String getPrefix() {
		return "MSG";
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public StringBuffer append(Object target) {
		return new StringBuffer(code).append(target);
	}
}

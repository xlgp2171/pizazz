package org.pizazz2.message.ref;

/**
 * 消息类型接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IType {
	/**
	 * 消息类型值
	 * @return 消息类型值
	 */
	String value();

	/**
	 * 转换为配置的key值
	 * @param key 消息对应key值
	 * @return 配置的key值
	 */
	String toConfigureKey(String key);

	/**
	 * 转换为国际化的key值
	 * @param key 消息对应key值
	 * @return 国际化key值
	 */
	public String toLocaleKey(String key);
}

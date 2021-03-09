package org.pizazz2.message.ref;

/**
 * 消息识别码接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IMessageCode {

	/**
	 * 获取信息前缀
	 * @return 前缀
	 */
	String getPrefix();

	/**
	 * 获取消息码
	 * @return 消息码
	 */
	String getCode();

	/**
	 * 追加数据
	 * @param target 数据（会转换成String）
	 * @return StringBuffer
	 */
	StringBuffer append(Object target);
}

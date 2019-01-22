package org.pizazz.message.ref;

/**
 * 消息识别码接口
 * 
 * @author xlgp2171
 * @version 1.0.190122
 */
public interface IMessageCode {

	public String getPrefix();

	public String getCode();

	public StringBuffer append(Object target);
}

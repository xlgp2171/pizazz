package org.pizazz.message;

/**
 * 消息识别码接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IMessageCode {

	public String getPrefix();

	public String getValue();

	public IMessageCode append(Object target);
}

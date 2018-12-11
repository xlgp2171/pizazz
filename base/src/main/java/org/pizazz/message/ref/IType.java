package org.pizazz.message.ref;

/**
 * 消息类型接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IType {

	public String value();

	public String toConfigureKey(String key);

	public String toLocaleKey(String key);
}

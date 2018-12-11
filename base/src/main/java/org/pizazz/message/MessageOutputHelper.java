package org.pizazz.message;

import org.pizazz.IMessageOutput;

/**
 * 消息输出工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public final class MessageOutputHelper {

	public static final IMessageOutput<String> EMPTY_STRING;

	static {
		EMPTY_STRING = new IMessageOutput<String>() {
			@Override
			public void write(String message) {
				message = null;
			}
		};
	}
}

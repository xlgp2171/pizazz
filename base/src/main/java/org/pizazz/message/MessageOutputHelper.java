package org.pizazz.message;

import org.pizazz.IMessageOutput;

/**
 * 消息输出工具
 * 
 * @author xlgp2171
 * @version 1.1.181216
 */
public final class MessageOutputHelper {

	public static final IMessageOutput<String> EMPTY_STRING;
	public static final IMessageOutput<String> EMPTY_STRING_ENABLE;

	static {
		EMPTY_STRING = new IMessageOutput<String>() {
			@Override
			public void write(String message) {
				message = null;
			}
		};
		EMPTY_STRING_ENABLE = new IMessageOutput<String>() {
			@Override
			public boolean isEnable() {
				return true;
			};

			@Override
			public void write(String message) {
				message = null;
			}
		};
	}
}

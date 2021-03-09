package org.pizazz2.message;

import org.pizazz2.message.ref.IType;

/**
 * 基础消息枚举
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public enum TypeEnum implements IType {
	/**
	 * 基础类型
	 */
	BASIC {
		@Override
		public String value() {
			return this.name();
		}
		
		@Override
		public String toConfigureKey(String key) {
			return value() + "_" + key;
		}

		@Override
		public String toLocaleKey(String key) {
			return value() + "." + key;
		}
	}
}

package org.pizazz.message.ref;

/**
 * 基础消息枚举
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public enum TypeEnum implements IType {
	BASIC {
		@Override
		public String value() {
			return this.name();
		}
		
		@Override
		public String toConfigureKey(String key) {
			return new StringBuilder(key).insert(4, value() + "_").toString();
		}

		@Override
		public String toLocaleKey(String key) {
			return value() + "." + key;
		}
	};
}

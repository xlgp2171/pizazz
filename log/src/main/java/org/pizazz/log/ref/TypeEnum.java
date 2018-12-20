package org.pizazz.log.ref;

import org.pizazz.message.ref.IType;

/**
 * 日志类型枚举
 *
 * @author xlgp2171
 * @version 1.0.181210
 */
public enum TypeEnum implements IType {
	LOG {
		@Override
		public String value() {
			return this.name();
		}

		@Override
		public String toConfigureKey(String key) {
			return value() + "." + key;
		}

		@Override
		public String toLocaleKey(String key) {
			return value() + "." + key;
		}
	};
}

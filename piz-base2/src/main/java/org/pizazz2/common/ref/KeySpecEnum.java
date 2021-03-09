package org.pizazz2.common.ref;

import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 密钥编码格式枚举
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public enum KeySpecEnum {
	/**
	 * PKCS8
	 */
	PKCS8 {
		@Override
		public EncodedKeySpec create(byte[] encodedKey) {
			return new PKCS8EncodedKeySpec(encodedKey);
		}
	},
	/**
	 * X509
	 */
	X509 {
		@Override
		public EncodedKeySpec create(byte[] encodedKey) {
			return new X509EncodedKeySpec(encodedKey);
		}
	};
	public abstract EncodedKeySpec create(byte[] encodedKey);
}

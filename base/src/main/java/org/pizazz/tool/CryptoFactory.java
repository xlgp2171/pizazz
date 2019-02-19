package org.pizazz.tool;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

import org.pizazz.IObject;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.CryptoUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.UtilityException;

/**
 * 加密解密工厂
 * 
 * @author xlgp2171
 * @version 1.1.190219
 */
public class CryptoFactory {

	public static class MD5Coder implements IObject {
		private final ByteBuffer target;

		public MD5Coder(byte[] data) {
			this(ByteBuffer.wrap(data));
		}

		public MD5Coder(ByteBuffer target) {
			this.target = target;
		}

		@Override
		public String getId() {
			return "MD5";
		}

		public byte[] digest() throws AssertException, UtilityException {
			return CryptoUtils.digest(getId(), target);
		}
	}

	public static class DESCoder implements IObject {
		private final SecretKey key;

		public DESCoder(byte[] key) throws AssertException, UtilityException {
			DESKeySpec _keySpec = CryptoUtils.newDESKeySpec(key);
			// 从原始密钥数据创建DESKeySpec对象
			this.key = CryptoUtils.newSecretKey(getId(), _keySpec);
		}

		@Override
		public String getId() {
			return "DES";
		}

		public byte[] encrypt(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.newCipher(getId(), key, Cipher.ENCRYPT_MODE, target);
		}

		public byte[] decrypt(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.newCipher(getId(), key, Cipher.DECRYPT_MODE, target);
		}
	}

	public static class BaseCodec implements IObject {
		protected final KeyFactory factory;
		protected final String algorithm;
		protected PublicKey publicKey;
		protected PrivateKey privateKey;
		private String signatureMode;

		public BaseCodec(String algorithm) throws AssertException, UtilityException {
			factory = CryptoUtils.newKeyFactory(algorithm);
			this.algorithm = algorithm;
		}

		@Override
		public String getId() {
			return algorithm;
		}

		public String getSignatureMode() {
			return signatureMode;
		}

		public BaseCodec setSignatureMode(String signatureMode) {
			this.signatureMode = signatureMode;
			return this;
		}

		public BaseCodec createKey(int size) throws AssertException, UtilityException {
			Key[] _keys = CryptoUtils.newKey(getId(), size);
			try {
				publicKey = ClassUtils.cast(_keys[0], PublicKey.class);
				privateKey = ClassUtils.cast(_keys[1], PrivateKey.class);
			} catch (BaseException e) {
			}
			return this;
		}

		/**
		 * 签名
		 * 
		 * @param target 源数据
		 * @return
		 * @throws UtilityException
		 * @throws AssertException
		 */
		public byte[] sign(byte[] target) throws AssertException, UtilityException {
			// 用私钥对信息生成数字签名
			return CryptoUtils.sign(getSignatureMode(), getPrivateKey(), ByteBuffer.wrap(target));
		}

		/**
		 * 验证
		 * 
		 * @param target 源数据
		 * @param signature 签名
		 * @return
		 * @throws AssertException
		 * @throws UtilityException
		 */
		public boolean verify(byte[] target, byte[] signature) throws AssertException, UtilityException {
			// 验证签名是否正常
			return CryptoUtils.verify(getSignatureMode(), getPublicKey(), ByteBuffer.wrap(target), signature);
		}

		/**
		 * 通过公钥加密
		 * 
		 * @param target
		 * @return
		 * @throws AssertException
		 * @throws UtilityException
		 */
		public byte[] encryptByPublic(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.encrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥加密
		 * 
		 * @param target
		 * @return
		 * @throws AssertException
		 * @throws UtilityException
		 */
		public byte[] encryptByPrivate(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.encrypt(getId(), target, getPrivateKey());
		}

		/**
		 * 通过公钥解码
		 * 
		 * @param target
		 * @return
		 * @throws UtilityException
		 * @throws AssertException
		 */
		public byte[] decryptByPublic(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.decrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥解码
		 * 
		 * @param target
		 * @return
		 * @throws AssertException
		 * @throws UtilityException
		 */
		public byte[] decryptByPrivate(byte[] target) throws AssertException, UtilityException {
			return CryptoUtils.decrypt(getId(), target, getPrivateKey());
		}

		public BaseCodec setPublicKey(byte[] key) throws AssertException, UtilityException {
			publicKey = CryptoUtils.toPublicKey(factory, key);
			return this;
		}

		public BaseCodec setPrivateKey(byte[] key) throws AssertException, UtilityException {
			privateKey = CryptoUtils.toPrivateKey(factory, key);
			return this;
		}

		public PublicKey getPublicKey() {
			return publicKey;
		}

		public PrivateKey getPrivateKey() {
			return privateKey;
		}
	}

	public static class RSACoder extends BaseCodec {
		public static final int SIZE = 1024;

		public RSACoder() throws AssertException, UtilityException {
			super("RSA");
		}

		@Override
		public String getSignatureMode() {
			return super.getSignatureMode() == null ? "MD5with" + getId() : super.getSignatureMode();
		}

		public BaseCodec createKey() throws AssertException, UtilityException {
			return super.createKey(SIZE);
		}
	}
}

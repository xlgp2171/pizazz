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
import org.pizazz.exception.BaseException;

/**
 * 加密解密工厂
 * 
 * @author xlgp2171
 * @version 1.0.181210
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

		public byte[] digest() throws BaseException {
			return CryptoUtils.digest(getId(), target);
		}
	}

	public static class DESCoder implements IObject {
		private final SecretKey key;

		public DESCoder(byte[] key) throws BaseException {
			DESKeySpec _keySpec = CryptoUtils.newDESKeySpec(key);
			// 从原始密钥数据创建DESKeySpec对象
			this.key = CryptoUtils.newSecretKey(getId(), _keySpec);
		}

		@Override
		public String getId() {
			return "DES";
		}

		public byte[] encrypt(byte[] target) throws BaseException {
			return CryptoUtils.newCipher(getId(), key, Cipher.ENCRYPT_MODE,
			        target);
		}

		public byte[] decrypt(byte[] target) throws BaseException {
			return CryptoUtils.newCipher(getId(), key, Cipher.DECRYPT_MODE,
			        target);
		}
	}

	public static class BaseCoder implements IObject {
		protected final KeyFactory factory;
		protected final String algorithm;
		protected PublicKey publicKey;
		protected PrivateKey privateKey;
		private String signatureMode;

		public BaseCoder(String algorithm) throws BaseException {
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

		public BaseCoder setSignatureMode(String signatureMode) {
			this.signatureMode = signatureMode;
			return this;
		}

		public BaseCoder createKey(int size) throws BaseException {
			Key[] _keys = CryptoUtils.newKey(getId(), size);
			try {
				publicKey = ClassUtils.cast(_keys[0], PublicKey.class);
				privateKey = ClassUtils.cast(_keys[1], PrivateKey.class);
			} catch (BaseException e) {}
			return this;
		}

		/**
		 * 签名
		 * @param target 源数据
		 * @return
		 * @throws BaseException
		 */
		public byte[] sign(byte[] target) throws BaseException {
			// 用私钥对信息生成数字签名
			return CryptoUtils.sign(getSignatureMode(), getPrivateKey(),
			        ByteBuffer.wrap(target));
		}

		/**
		 * 验证
		 * @param target 源数据
		 * @param signature 签名
		 * @return
		 * @throws BaseException
		 */
		public boolean verify(byte[] target, byte[] signature)
		        throws BaseException {
			// 验证签名是否正常
			return CryptoUtils.verify(getSignatureMode(), getPublicKey(),
			        ByteBuffer.wrap(target), signature);
		}

		/**
		 * 通过公钥加密
		 * @param target
		 * @return
		 * @throws BaseException
		 */
		public byte[] encryptByPublic(byte[] target) throws BaseException {
			return CryptoUtils.encrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥加密
		 * @param target
		 * @return
		 * @throws BaseException
		 */
		public byte[] encryptByPrivate(byte[] target) throws BaseException {
			return CryptoUtils.encrypt(getId(), target, getPrivateKey());
		}

		/**
		 * 通过公钥解码
		 * @param target
		 * @return
		 * @throws BaseException
		 */
		public byte[] decryptByPublic(byte[] target) throws BaseException {
			return CryptoUtils.decrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥解码
		 * @param target
		 * @return
		 * @throws BaseException
		 */
		public byte[] decryptByPrivate(byte[] target) throws BaseException {
			return CryptoUtils.decrypt(getId(), target, getPrivateKey());
		}

		public BaseCoder setPublicKey(byte[] key) throws BaseException {
			publicKey = CryptoUtils.toPublicKey(factory, key);
			return this;
		}

		public BaseCoder setPrivateKey(byte[] key) throws BaseException {
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

	public static class RSACoder extends BaseCoder {
		public static final int SIZE = 1024;

		public RSACoder() throws BaseException {
			super("RSA");
		}

		@Override
		public String getSignatureMode() {
			return super.getSignatureMode() == null ? "MD5with" + getId()
			        : super.getSignatureMode();
		}

		public BaseCoder createKey() throws BaseException {
			return super.createKey(SIZE);
		}
	}
}

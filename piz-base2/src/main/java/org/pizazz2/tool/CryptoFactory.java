package org.pizazz2.tool;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

import org.pizazz2.IObject;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.common.CryptoUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;

/**
 * 加密解密工厂
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CryptoFactory {

	/**
	 * MD5编码器
	 * <pre>
	 * new MD5Coder("内容".getBytes()).digest();
	 * </pre>
	 */
	public static class MD5Coder implements IObject {
		private final ByteBuffer target;

		public MD5Coder(byte[] data) throws ValidateException {
			ValidateUtils.notEmpty(data, "MD5Coder");
			this.target = ByteBuffer.wrap(data);
		}

		public MD5Coder(ByteBuffer target) {
			this.target = target;
		}

		@Override
		public String getId() {
			return "MD5";
		}

		public byte[] digest() throws ValidateException, UtilityException {
			return CryptoUtils.digest(getId(), target);
		}
	}

	/**
	 * DES编码器
	 */
	public static class DESCoder implements IObject {
		private final SecretKey key;

		public DESCoder(byte[] key) throws ValidateException, UtilityException {
			DESKeySpec keySpec = CryptoUtils.newDESKeySpec(key);
			// 从原始密钥数据创建DESKeySpec对象
			this.key = CryptoUtils.newSecretKey(getId(), keySpec);
		}

		@Override
		public String getId() {
			return "DES";
		}

		/**
		 * 加密
		 * @param target 加密内容
		 * @return 加密后的内容
		 * @throws ValidateException 验证问题
		 * @throws UtilityException 加密异常
		 */
		public byte[] encrypt(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.newCipher(getId(), key, Cipher.ENCRYPT_MODE, target);
		}

		/**
		 * 解密
		 * @param target 解密内容
		 * @return 解密后的原始内容
		 * @throws ValidateException 验证问题
		 * @throws UtilityException 解密异常
		 */
		public byte[] decrypt(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.newCipher(getId(), key, Cipher.DECRYPT_MODE, target);
		}
	}

	/**
	 * 非对称编码器
	 */
	public static class AsymmetricCodec implements IObject {
		protected final KeyFactory factory;
		protected final String algorithm;
		protected final String signatureAlgorithm;
		protected PublicKey publicKey;
		protected PrivateKey privateKey;

		/**
		 * 非对称编码器
		 * @param algorithm 算法
		 * @param signatureAlgorithm 签名算法
		 * @throws ValidateException 验证异常
		 * @throws UtilityException 公钥私钥工厂加载失败
		 */
		public AsymmetricCodec(String algorithm, String signatureAlgorithm) throws ValidateException, UtilityException {
			factory = CryptoUtils.newKeyFactory(algorithm);
			this.algorithm = algorithm;
			this.signatureAlgorithm = signatureAlgorithm;
		}

		@Override
		public String getId() {
			return algorithm;
		}

		public AsymmetricCodec createKey(int size) throws ValidateException, UtilityException {
			Key[] keys = CryptoUtils.newKey(getId(), size);
			publicKey = ClassUtils.cast(keys[0], PublicKey.class);
			privateKey = ClassUtils.cast(keys[1], PrivateKey.class);
			return this;
		}

		/**
		 * 使用编码器签名
		 * 
		 * @param target 源数据
		 * @return 签名内容
		 * @throws UtilityException 签名无效或算法无效
		 * @throws ValidateException 验证异常
		 */
		public byte[] sign(byte[] target) throws ValidateException, UtilityException {
			// 用私钥对信息生成数字签名
			return CryptoUtils.sign(signatureAlgorithm, getPrivateKey(), ByteBuffer.wrap(target));
		}

		/**
		 * 验证
		 * 
		 * @param target 源数据
		 * @param signature 签名
		 * @return 是否雅正成功
		 * @throws ValidateException 参数验证异常
		 * @throws UtilityException 签名无效或算法无效
		 */
		public boolean verify(byte[] target, byte[] signature) throws ValidateException, UtilityException {
			// 验证签名是否正常
			return CryptoUtils.verify(signatureAlgorithm, getPublicKey(), ByteBuffer.wrap(target), signature);
		}

		/**
		 * 通过公钥加密
		 * 
		 * @param target 需要加密数据
		 * @return 加密后数据
		 * @throws ValidateException 参数验证异常
		 * @throws UtilityException 加密异常
		 */
		public byte[] encryptByPublic(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.encrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥加密
		 * 
		 * @param target 需要加密数据
		 * @return 需要加密数据
		 * @throws ValidateException 参数验证异常
		 * @throws UtilityException 加密异常
		 */
		public byte[] encryptByPrivate(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.encrypt(getId(), target, getPrivateKey());
		}

		/**
		 * 通过公钥解码
		 * 
		 * @param target 需要解密数据
		 * @return 需要解密数据
		 * @throws UtilityException 解密异常
		 * @throws ValidateException 参数验证异常
		 */
		public byte[] decryptByPublic(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.decrypt(getId(), target, getPublicKey());
		}

		/**
		 * 通过私钥解码
		 * 
		 * @param target 需要解密数据
		 * @return 需要解密数据
		 * @throws UtilityException 解密异常
		 * @throws ValidateException 参数验证异常
		 */
		public byte[] decryptByPrivate(byte[] target) throws ValidateException, UtilityException {
			return CryptoUtils.decrypt(getId(), target, getPrivateKey());
		}

		public AsymmetricCodec setPublicKey(byte[] key) throws ValidateException, UtilityException {
			publicKey = CryptoUtils.toPublicKey(factory, key);
			return this;
		}

		public AsymmetricCodec setPrivateKey(byte[] key) throws ValidateException, UtilityException {
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

	/**
	 * RSA编码器
	 */
	public static class RSACoder extends AsymmetricCodec {
		public static final int SIZE = 1024;

		public RSACoder() throws ValidateException, UtilityException {
			super("RSA", "MD5withRSA");
			super.createKey(SIZE);
		}
	}
}

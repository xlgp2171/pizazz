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

	public static class BaseCoder {
		private final ByteBuffer target;
		private final String algorithm;

		public BaseCoder(String algorithm, byte[] data) throws ValidateException {
			ValidateUtils.notEmpty("BaseCoder", algorithm);
			ValidateUtils.notEmpty("BaseCoder", data, 2);
			this.algorithm = algorithm;
			this.target = ByteBuffer.wrap(data);
		}

		public String getId() {
			return algorithm;
		}

		public byte[] digest() throws ValidateException {
			try {
				return CryptoUtils.digest(getId(), target);
			} catch(UtilityException e) {
				throw new ValidateException(e.getMessage(), e);
			}
		}

		@Override
		public String toString() throws ValidateException {
			byte[] data = digest();
			return CryptoUtils.encodeBase64ToString(data);
		}
	}
	/**
	 * MD5编码器
	 * <pre>
	 * new MD5Coder("内容".getBytes()).digest();
	 * </pre>
	 */
	public static class MD5Coder extends BaseCoder {
		public MD5Coder(byte[] data) throws ValidateException {
			super("MD5", data);
		}
	}

	public static class SHA512Coder extends BaseCoder {
		public SHA512Coder(byte[] data) throws ValidateException {
			super("SHA-512", data);
		}
	}

	public static class SymmetricCoder {
		private final SecretKey key;
		private final String algorithm;

		public SymmetricCoder(String algorithm, byte[] key) throws ValidateException, UtilityException {
			ValidateUtils.notEmpty("SymmetricCoder", algorithm );
			ValidateUtils.notEmpty("SymmetricCoder", key, 2);
			this.algorithm = algorithm;
			DESKeySpec keySpec = CryptoUtils.newDESKeySpec(key);
			// 从原始密钥数据创建DESKeySpec对象
			this.key = CryptoUtils.newSecretKey(getId(), keySpec);
		}

		public String getId() {
			return algorithm;
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
	 * DES编码器
	 */
	public static class DESCoder extends SymmetricCoder {
		public DESCoder(byte[] key) throws ValidateException, UtilityException {
			super("DES", key);
		}
	}

	/**
	 * 非对称编码器
	 */
	public static class AsymmetricCodec<E extends CryptoFactory.AsymmetricCodec<?>> {
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
			ValidateUtils.notEmpty("AsymmetricCodec", algorithm);
			ValidateUtils.notEmpty("AsymmetricCodec", signatureAlgorithm, 2);
			factory = CryptoUtils.newKeyFactory(algorithm);
			this.algorithm = algorithm;
			this.signatureAlgorithm = signatureAlgorithm;
		}

		public String getId() {
			return algorithm;
		}

		@SuppressWarnings("unchecked")
		public E createKey(int size) throws ValidateException, UtilityException {
			Key[] keys = CryptoUtils.newKey(getId(), size);
			publicKey = ClassUtils.cast(keys[0], PublicKey.class);
			privateKey = ClassUtils.cast(keys[1], PrivateKey.class);
			return (E) this;
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
			ValidateUtils.notEmpty("sign", target);
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
			ValidateUtils.notEmpty("verify", target);
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

		@SuppressWarnings("unchecked")
		public E setPublicKey(byte[] key) throws ValidateException, UtilityException {
			publicKey = CryptoUtils.toPublicKey(factory, key);
			return (E) this;
		}

		@SuppressWarnings("unchecked")
		public E setPrivateKey(byte[] key) throws ValidateException, UtilityException {
			privateKey = CryptoUtils.toPrivateKey(factory, key);
			return (E) this;
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
	public static class RSACoder extends AsymmetricCodec<RSACoder> {
		public static final int SIZE = 1024;

		public RSACoder() throws ValidateException, UtilityException {
			super("RSA", "MD5withRSA");
		}
	}
}

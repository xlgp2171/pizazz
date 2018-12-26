package org.pizazz.common;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.pizazz.common.ref.KeySpecEnum;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 加密解密工具
 *
 * @author xlgp2171
 * @version 1.0.181224
 */
public class CryptoUtils {
	public static String encodeBase64String(byte[] target) {
		return Base64.getEncoder().encodeToString(target);
	}

	public static byte[] encodeBase64(byte[] target) {
		return Base64.getEncoder().encode(target);
	}

	public static byte[] decodeBase64(String target) {
		return Base64.getDecoder().decode(target);
	}

	public static byte[] decodeBase64(byte[] target) {
		return Base64.getDecoder().decode(target);
	}

	public static String encodeBase32(byte[] target) {
		return new org.apache.commons.codec.binary.Base32().encodeAsString(target);
	}

	public static byte[] decodeBase32(String target) {
		return new org.apache.commons.codec.binary.Base32().decode(target);
	}

	public static SecretKey newSecretKey(String algorithm, KeySpec keySpec) throws BaseException {
		AssertUtils.assertNotNull("newSecretKey", algorithm, keySpec);
		// 创建一个密钥工厂
		try {
			return SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", algorithm,
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static byte[] newCipher(String algorithm, Key key, int opmode, byte[] data) throws BaseException {
		AssertUtils.assertNotNull("newCipher", algorithm, key, 0, data);
		Cipher _cipher;
		try {
			// Cipher对象实际完成加密操作
			_cipher = Cipher.getInstance(algorithm);
			// 用密钥初始化Cipher对象,生成一个可信任的随机数源
			_cipher.init(opmode, key, new SecureRandom());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", algorithm,
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
		try {
			return _cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.CIPHER.NEW", algorithm, e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static Key[] newKey(String algorithm, int size) throws BaseException {
		AssertUtils.assertNotNull("newKey", algorithm);
		KeyPairGenerator _generator;
		try {
			_generator = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", algorithm,
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
		_generator.initialize(size);
		KeyPair _key = _generator.generateKeyPair();
		Key[] _tmp = new Key[2];
		// 公钥
		_tmp[0] = _key.getPublic();
		// 私钥
		_tmp[1] = _key.getPrivate();
		return _tmp;
	}

	public static DESKeySpec newDESKeySpec(byte[] key) throws BaseException {
		AssertUtils.assertLength("newDESKeySpec", 1, key, 8);
		try {
			return new DESKeySpec(key);
		} catch (InvalidKeyException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", "DES", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static KeyFactory newKeyFactory(String algorithm) throws BaseException {
		AssertUtils.assertNotNull("newKeyFactory", algorithm);
		try {
			return KeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", "DES", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static PrivateKey toPrivateKey(KeyFactory factory, byte[] encodedKey) throws BaseException {
		AssertUtils.assertNotNull("toPrivateKey", factory, encodedKey);
		try {
			return factory.generatePrivate(KeySpecEnum.PKCS8.create(encodedKey));
		} catch (InvalidKeySpecException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.VALID", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static PublicKey toPublicKey(KeyFactory factory, byte[] encodedKey) throws BaseException {
		AssertUtils.assertNotNull("toPrivateKey", factory, encodedKey);
		try {
			return factory.generatePublic(KeySpecEnum.X509.create(encodedKey));
		} catch (InvalidKeySpecException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.VALID", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static byte[] digest(String algorithm, ByteBuffer target) throws BaseException {
		AssertUtils.assertNotNull("digest", algorithm, target);
		MessageDigest _digest;
		try {
			// 获得摘要算法的 MessageDigest 对象
			_digest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SECRETKEY.NEW", algorithm,
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
		// 使用指定的字节更新摘要
		_digest.update(target);
		// 获得密文
		return _digest.digest();
	}

	/**
	 * 加密
	 * 
	 * @param algorithm
	 * @param target 源数据
	 * @param key
	 * @return
	 * @throws BaseException
	 */
	public static byte[] encrypt(String algorithm, byte[] target, Key key) throws BaseException {
		return newCipher(algorithm, key, Cipher.ENCRYPT_MODE, target);
	}

	/**
	 * 解密
	 * 
	 * @param algorithm
	 * @param target 加密数据
	 * @param key
	 * @return
	 * @throws BaseException
	 */
	public static byte[] decrypt(String algorithm, byte[] target, Key key) throws BaseException {
		return newCipher(algorithm, key, Cipher.DECRYPT_MODE, target);
	}

	/**
	 * 数字签名<br>
	 * 算法包括
	 * <li>MD2withRSA
	 * <li>MD5withRSA
	 * <li>SHA1withRSA
	 * <li>SHA224withRSA
	 * <li>SHA256withRSA
	 * <li>SHA384withRSA
	 * <li>SHA512withRSA
	 * <li>RIPEMD123withRSA
	 * <li>RIPEMD160withRSA
	 * <li>SHA1withDSA
	 * <li>SHA224withDSA
	 * <li>SHA256withDSA
	 * <li>SHA384withDSA
	 * <li>SHA512withDSA
	 * <li>NONEwithCDSA
	 * <li>RIPEMD160withCDSA
	 * <li>SHA1withCDSA
	 * <li>SHA224withCDSA
	 * <li>SHA256withCDSA
	 * <li>SHA384withCDSA
	 * <li>SHA512withCDSA
	 * 
	 * @param signature 算法
	 * @param key 私钥
	 * @param data 需要数字签名的数据
	 * @return
	 * @throws BaseException
	 */
	public static byte[] sign(String signature, PrivateKey key, ByteBuffer data) throws BaseException {
		AssertUtils.assertNotNull("sign", signature, key, data);
		Signature _signature;
		try {
			_signature = Signature.getInstance(signature);
			_signature.initSign(key);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.NEW", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
		try {
			_signature.update(data);
			return _signature.sign();
		} catch (SignatureException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.NEW", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}

	public static boolean verify(String signature, PublicKey key, ByteBuffer data, byte[] sign) throws BaseException {
		AssertUtils.assertNotNull("verify", signature, key, data, sign);
		Signature _signature;
		try {
			_signature = Signature.getInstance(signature);
			_signature.initVerify(key);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.VALID", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
		try {
			_signature.update(data);
			// 验证签名是否正常
			return _signature.verify(sign);
		} catch (SignatureException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.VALID", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0015, _msg, e);
		}
	}
}

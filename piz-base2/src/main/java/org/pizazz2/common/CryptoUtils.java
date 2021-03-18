package org.pizazz2.common;

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

import org.pizazz2.common.ref.KeySpecEnum;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 加密解密工具
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CryptoUtils {
    public static String encodeBase64ToString(byte[] target) {
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

    public static String encodeBase32ToString(byte[] target) {
        return new org.apache.commons.codec.binary.Base32().encodeAsString(target);
    }

    public static byte[] encodeBase32(byte[] target) {
        return new org.apache.commons.codec.binary.Base32().encode(target);
    }

    public static byte[] decodeBase32(String target) {
        return new org.apache.commons.codec.binary.Base32().decode(target);
    }

    public static byte[] decodeBase32(byte[] target) {
        return new org.apache.commons.codec.binary.Base32().decode(target);
    }

    /**
     * 生成密钥包装类
     *
     * @param algorithm 算法
     * @param keySpec 密钥类型
     * @return 密钥包装类
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 生成异常
     */
    public static SecretKey newSecretKey(String algorithm, KeySpec keySpec) throws ValidateException, UtilityException {
        ValidateUtils.notNull("newSecretKey", algorithm, keySpec);
        // 创建一个密钥工厂
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", algorithm, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    public static byte[] newCipher(String algorithm, Key key, int mode, byte[] data) throws ValidateException, UtilityException {
        ValidateUtils.notNull("newCipher", algorithm, key, 0, data);
        Cipher cipher;
        try {
            // Cipher对象实际完成加密操作
            cipher = Cipher.getInstance(algorithm);
            // 用密钥初始化Cipher对象,生成一个可信任的随机数源
            cipher.init(mode, key, new SecureRandom());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", algorithm, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.CIPHER.NEW", algorithm, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    /**
     * 公钥私钥生成器
     *
     * @param algorithm 算法
     * @param size 密钥长度
     * @return 索引0为公钥，索引1为私钥
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 公钥私钥生成异常
     */
    public static Key[] newKey(String algorithm, int size) throws ValidateException, UtilityException {
        ValidateUtils.notNull("newKey", algorithm);
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", algorithm, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
        generator.initialize(size);
        KeyPair key = generator.generateKeyPair();
        Key[] tmp = new Key[2];
        // 公钥
        tmp[0] = key.getPublic();
        // 私钥
        tmp[1] = key.getPrivate();
        return tmp;
    }

    /**
     * DES密钥生成
     *
     * @param data 密钥内容 8位
     * @return DES密钥
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException DES密钥生成异常
     */
    public static DESKeySpec newDESKeySpec(byte[] data) throws ValidateException, UtilityException {
        ValidateUtils.sameLength("newDESKeySpec", 1, data, 8);
        try {
            return new DESKeySpec(data);
        } catch (InvalidKeyException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", "DES", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    /**
     * 构建公钥私钥工厂
     *
     * @param algorithm 算法
     * @return 公钥私钥工厂
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 公钥私钥工厂加载失败
     */
    public static KeyFactory newKeyFactory(String algorithm) throws ValidateException, UtilityException {
        ValidateUtils.notNull("newKeyFactory", algorithm);
        try {
            return KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", "DES", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    public static PrivateKey toPrivateKey(KeyFactory factory, byte[] encodedKey) throws ValidateException, UtilityException {
        ValidateUtils.notNull("toPrivateKey", factory, encodedKey);
        try {
            return factory.generatePrivate(KeySpecEnum.PKCS8.create(encodedKey));
        } catch (InvalidKeySpecException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.VALID", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    public static PublicKey toPublicKey(KeyFactory factory, byte[] encodedKey) throws ValidateException, UtilityException {
        ValidateUtils.notNull("toPrivateKey", factory, encodedKey);
        try {
            return factory.generatePublic(KeySpecEnum.X509.create(encodedKey));
        } catch (InvalidKeySpecException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.VALID", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    /**
     * 加密内容
     * <li/>MD5
     * <li/>SHA
     *
     * @param algorithm 算法
     * @param data 加密数据
     * @return 加密结果
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 加密异常
     */
    public static byte[] digest(String algorithm, ByteBuffer data) throws ValidateException, UtilityException {
        ValidateUtils.notNull("digest", algorithm, data);
        MessageDigest digest;
        try {
            // 获得摘要算法的 MessageDigest 对象
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.KEYS.NEW", algorithm, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
        // 使用指定的字节更新摘要
        digest.update(data);
        // 获得密文
        return digest.digest();
    }

    /**
     * 加密
     *
     * @param algorithm 算法
     * @param target 源数据
     * @param key 加密密钥类型
     * @return 公钥或私钥内容
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 加密异常
     */
    public static byte[] encrypt(String algorithm, byte[] target, Key key) throws ValidateException, UtilityException {
        return CryptoUtils.newCipher(algorithm, key, Cipher.ENCRYPT_MODE, target);
    }

    /**
     * 解密
     *
     * @param algorithm 算法
     * @param target 加密数据
     * @param key 解密密钥类型
     * @return 公钥或私钥内容
     *
     * @throws UtilityException 解密异常
     * @throws ValidateException 参数验证异常
     */
    public static byte[] decrypt(String algorithm, byte[] target, Key key) throws ValidateException, UtilityException {
        return CryptoUtils.newCipher(algorithm, key, Cipher.DECRYPT_MODE, target);
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
     * @param algorithm 算法
     * @param key 私钥
     * @param data 需要数字签名的数据
     * @return 签名内容
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 签名无效或算法无效
     */
    public static byte[] sign(String algorithm, PrivateKey key, ByteBuffer data) throws ValidateException, UtilityException {
        ValidateUtils.notNull("sign", algorithm, key, data);
        Signature signature;
        try {
            signature = Signature.getInstance(algorithm);
            signature.initSign(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.NEW", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
        try {
            signature.update(data);
            return signature.sign();
        } catch (SignatureException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.NEW", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }

    /**
     * 验证数字签名
     *
     * @param algorithm 签名算法
     * @param key 公钥对象
     * @param data 源签名内容
     * @param signature 需要验证的签名内容
     * @return 是否正确的签名
     *
     * @throws ValidateException 参数验证异常
     * @throws UtilityException 签名无效或算法无效
     */
    public static boolean verify(String algorithm, PublicKey key, ByteBuffer data, byte[] signature) throws ValidateException, UtilityException {
        ValidateUtils.notNull("verify", algorithm, key, data);
        ValidateUtils.notEmpty("verify", signature, 4);
        Signature tmp;
        try {
            tmp = Signature.getInstance(algorithm);
            tmp.initVerify(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.VALID", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
        try {
            // 装载签名
            tmp.update(data);
            // 验证签名是否正确
            return tmp.verify(signature);
        } catch (SignatureException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CRYPTO.SIGNATURE.VALID", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0015, msg, e);
        }
    }
}

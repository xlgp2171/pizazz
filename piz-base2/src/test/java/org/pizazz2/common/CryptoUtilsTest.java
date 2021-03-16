package org.pizazz2.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.tool.CryptoFactory;

/**
 * CryptoUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CryptoUtilsTest {

	public static final String MSG = "中文信息";
	// 8位以上
	public static final String KEY = "!@#$%^&*";

	@Test
	public void testMD5() throws UtilityException {
		byte[] data = CryptoUtils.digest("MD5", ByteBuffer.wrap(MSG.getBytes(StandardCharsets.UTF_8)));
		String result = CryptoUtils.encodeBase64ToString(data);
		Assert.assertEquals(result, "apmpF3CXu7zZOGPDWEIrDw==");
	}

	@Test
	public void testDES() throws UtilityException {
		byte[] tmp = new CryptoFactory.DESCoder(KEY.getBytes()).encrypt(MSG.getBytes());
		byte[] result = new CryptoFactory.DESCoder(KEY.getBytes()).decrypt(tmp);
		Assert.assertEquals(new String(result), MSG);
	}

	@Test
	public void testSignature() throws UtilityException {
		String algorithm = "RSA";
		Key[] keys = CryptoUtils.newKey(algorithm, 1024);
		KeyFactory factory = CryptoUtils.newKeyFactory(algorithm);
		PublicKey publicK = CryptoUtils.toPublicKey(factory, keys[0].getEncoded());
		PrivateKey privateK = CryptoUtils.toPrivateKey(factory, keys[1].getEncoded());
		byte[] sign = CryptoUtils.sign("SHA256withRSA", privateK, ByteBuffer.wrap(MSG.getBytes()));
		boolean result = CryptoUtils.verify("SHA256withRSA", publicK, ByteBuffer.wrap(MSG.getBytes()), sign);
		Assert.assertTrue(result);
	}

	@Test
	public void testRSASimple() throws UtilityException {
		CryptoFactory.AsymmetricCodec<?> asymmetric = new CryptoFactory.AsymmetricCodec<>("RSA", "MD5withRSA");
		byte[] data = asymmetric.createKey(512).encryptByPrivate(MSG.getBytes());
		data = asymmetric.decryptByPublic(data);
		Assert.assertEquals(new String(data), MSG);
	}
}

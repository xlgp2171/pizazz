package org.pizazz2.common;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.BaseException;
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
		byte[] msg = new CryptoFactory.MD5Coder(MSG.getBytes()).digest();
		String result = CryptoUtils.encodeBase64ToString(msg);
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
	public void testSignatureSimple() throws UtilityException {
		CryptoFactory.RSACoder rsa = new CryptoFactory.RSACoder();
		byte[] sign = rsa.sign(MSG.getBytes());
		boolean result = rsa.verify(MSG.getBytes(), sign);
		Assert.assertTrue(result);
	}


	@Test
	public void testRSASimple() throws UtilityException {
		CryptoFactory.AsymmetricCodec asymmetric = new CryptoFactory.AsymmetricCodec("RSA", "MD5withRSA");
		byte[] data = asymmetric.createKey(512).encryptByPrivate(MSG.getBytes());
		data = asymmetric.decryptByPublic(data);
		Assert.assertEquals(new String(data), MSG);
	}
/* FIXME 转移到Factory测试中去
	@Test
	public void testRSAByBase64() throws BaseException {
		Key[] _keys = CryptoUtils.newKey("RSA", 1024);
		byte[] _data = new CryptoFactory.BaseCodec("RSA").setPrivateKey(base64(_keys[1].getEncoded()))
				.encryptByPrivate(MSG.getBytes());
		_data = new CryptoFactory.BaseCodec("RSA").setPublicKey(base64(_keys[0].getEncoded())).decryptByPublic(_data);
		AssertUtils.assertEquals("testRSAByBase64", new String(_data), MSG);
	}

	@Test
	public void testRSA3() throws BaseException {
		String _name = "TestUser";
		Key[] _keysS = CryptoUtils.newKey("RSA", 512);
		CryptoFactory.BaseCodec _server = new CryptoFactory.BaseCodec("RSA");
		// 生成签名
		byte[] _sign = _server.setPrivateKey(base64(_keysS[1].getEncoded()))
				.setPublicKey(base64(_keysS[0].getEncoded())).setSignatureMode("MD5withRSA").sign(_name.getBytes());
		System.out.println(CryptoUtils.encodeBase64String(_sign));
		Key[] _keysC = CryptoUtils.newKey("RSA", 1024);
		CryptoFactory.BaseCodec _client = new CryptoFactory.BaseCodec("RSA");
		// 加密签名
		byte[] _data = _client.setPrivateKey(base64(_keysC[1].getEncoded())).encryptByPrivate(base64(_sign));
		// 解密签名
		_sign = new CryptoFactory.BaseCodec("RSA").setPublicKey(base64(_keysC[0].getEncoded()))
				.decryptByPublic(base64(_data));
		System.out.println(CryptoUtils.encodeBase64ToString(_sign));
		AssertUtils.assertTrue("testRSA3", _server.verify(_name.getBytes(), base64(_sign)));
	}

	private byte[] base64(byte[] target) {
		String _tmp = CryptoUtils.encodeBase64ToString(target);
		return CryptoUtils.decodeBase64(_tmp);
	}

	@Test
	public void testSHA256() throws BaseException {
		String _key = "2018.02.26_95_28";
		byte[] _result = CryptoUtils.digest("SHA-256", ByteBuffer.wrap(_key.getBytes()));
		System.out.println(StringUtils.toHexString(_result, ""));
		System.out.println(CryptoUtils.encodeBase64ToString(_result));
		System.out.println(CryptoUtils.encodeBase32ToString(_result));
	}

 */
}

package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.CryptoUtils;
import org.pizazz2.exception.UtilityException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * CryptoFactory测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CryptoFactoryTest {
    public static final String KEY = "!@#$%^&*";
    public static final String MSG = "中文信息";

    @Test
    public void testRSAEncryptAndDecrypt() throws UtilityException {
        CryptoFactory.RSACoder source = new CryptoFactory.RSACoder().createKey(1024);
        byte[] code = source.getPublicKey().getEncoded();
        byte[] data = source.encryptByPrivate(MSG.getBytes());
        CryptoFactory.RSACoder target = new CryptoFactory.RSACoder().setPublicKey(code);
        byte[] result = target.decryptByPublic(data);
        Assert.assertEquals(MSG, new String(result));
    }

    @Test
    public void testRSASignature() throws UtilityException {
        CryptoFactory.RSACoder source = new CryptoFactory.RSACoder().createKey(1024);
        byte[] sign = source.sign(MSG.getBytes());
        byte[] code = source.getPublicKey().getEncoded();
        CryptoFactory.RSACoder target = new CryptoFactory.RSACoder().setPublicKey(code);
        boolean result = target.verify(MSG.getBytes(), sign);
        Assert.assertTrue(result);
    }

    @Test
    public void testDES() throws UtilityException {
        CryptoFactory.DESCoder source = new CryptoFactory.DESCoder(KEY.getBytes());
        byte[] code = source.encrypt(MSG.getBytes());
        CryptoFactory.DESCoder target = new CryptoFactory.DESCoder(KEY.getBytes());
        byte[] data = target.decrypt(code);
        Assert.assertEquals(MSG, new String(data));
    }

    @Test
    public void testMD5() {
        String result = new CryptoFactory.MD5Coder(MSG.getBytes(StandardCharsets.UTF_8)).toString();
        Assert.assertEquals(result, "apmpF3CXu7zZOGPDWEIrDw==");
    }

    @Test
    public void testSHA512() {
        String result = new CryptoFactory.SHA512Coder(MSG.getBytes(StandardCharsets.UTF_8)).toString();
        Assert.assertEquals(result, "zg8SgcAS4rsYoZ04i55d6/15SHATo2t3RQgrhhYpl90wdfeMzrnTmyGZWIMh8Bf4xsVIV3hRgYRLhjC1JCvZlA==");
    }
}

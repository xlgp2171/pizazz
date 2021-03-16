package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * IOUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class IOUtilsTest {

	@Test
	public void testGetResourceAsStreamAndReadLine() throws UtilityException {
		InputStream in = IOUtils.getResourceAsStream("DynamicObject.tmp");
		List<String> result = IOUtils.readLine(in, StandardCharsets.UTF_8);
		Assert.assertEquals(result.get(0), "package org.pizazz2.test;");
	}

	@Test
	public void testToByteArray() throws UtilityException {
		InputStream in = IOUtils.getResourceAsStream("DynamicObject.tmp");
		byte[] data = IOUtils.toByteArray(in);
		Assert.assertEquals(335, data.length, 1);
	}
}

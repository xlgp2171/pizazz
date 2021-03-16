package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.util.Properties;

/**
 * ResourceUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ResourceUtilsTest {

	@Test
	public void testMergeProperties() throws UtilityException {
		String target = "MESSAGE_PARAM2";
		Properties tmp = new Properties();
		tmp.setProperty("message.param2", target);
		tmp = ResourceUtils.mergeProperties(tmp, "message.properties");
		Assert.assertEquals(tmp.getProperty("message.param2"), target);
	}

	@Test
	public void testGetDouble() throws UtilityException {
		Properties tmp = ResourceUtils.loadProperties("message.properties");
		double result = ResourceUtils.getDouble(tmp, "message.param3", 0.1);
		Assert.assertEquals(result, 23.4, 0);
	}

	@Test
	public void testGetBoolean() throws UtilityException {
		Properties tmp = ResourceUtils.loadProperties("message.properties");
		boolean result = ResourceUtils.getBoolean(tmp, "message.param1", false);
		Assert.assertTrue(result);
	}
}

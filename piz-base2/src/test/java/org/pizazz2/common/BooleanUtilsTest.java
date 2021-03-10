package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * BooleanUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class BooleanUtilsTest {

	@Test
	public void testToBoolean() {
		boolean result = BooleanUtils.toBoolean("1", false);
		Assert.assertTrue(result);
	}

	@Test
	public void testToBoolean1() {
		boolean result = BooleanUtils.toBoolean("true");
		Assert.assertTrue(result);
	}
}

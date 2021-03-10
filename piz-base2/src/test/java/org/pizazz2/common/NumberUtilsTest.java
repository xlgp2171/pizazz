package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.net.InetAddress;

/**
 * NumberUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class NumberUtilsTest {

	@Test
	public void testRandom() {
		double target = 12.1;
		double num = 2.5;
		double result = NumberUtils.random(target, target + num);
		Assert.assertEquals(result, target, num);
	}
}

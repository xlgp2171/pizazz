package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * NumberUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210804
 */
public class NumberUtilsTest {

	@Test
	public void testValidate() {
		boolean result = NumberUtils.validate(321, 123L, 456L);
		Assert.assertTrue(result);
	}

	@Test
	public void testToInt() {
		int result = NumberUtils.toInt("0x0008", 16, 16);
		Assert.assertEquals(result, 16);
	}

	@Test
	public void testGetType() {
		Class<?> result = NumberUtils.getType(new BigDecimal(1097110568869782150L));
		Assert.assertEquals(Long.TYPE, result);
	}

	@Test
	public void testToPlainString() {
		String result = NumberUtils.toPlainString(123.0987654321, 5);
		Assert.assertEquals(result, "123.09877");
	}

	@Test
	public void testRound() {
		double result = NumberUtils.round(12.3456, 3);
		Assert.assertEquals(result,12.346, 0);
	}

	@Test
	public void testRandom() {
		double target = 12.1;
		double num = 2.5;
		double result = NumberUtils.random(target, target + num);
		Assert.assertEquals(result, target, num);
	}
}

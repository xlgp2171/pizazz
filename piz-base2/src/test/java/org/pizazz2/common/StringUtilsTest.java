package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.message.ExpressionEnum;

/**
 * StringUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class StringUtilsTest {

	@Test
	public void testIsTrimEmpty() {
		boolean result = StringUtils.isTrimEmpty(" ");
		Assert.assertTrue(result);
	}

	@Test
	public void testFormat() {
		String result = StringUtils.format("'' {1} ''异常为{0}", "IOException", "TYPE");
		Assert.assertEquals(result, "' TYPE '异常为IOException");
	}

	@Test
	public void testJoin() {
		String result = StringUtils.join(new String[]{"AB", "CD", "12", "34"}, "-");
		Assert.assertEquals(result, "AB-CD-12-34");
	}

	@Test
	public void testRepeatString() {
		String result = StringUtils.repeatString("0", 8);
		Assert.assertEquals(result, "00000000");
	}

	@Test
	public void testFillAndReplace() {
		String result = StringUtils.fillAndReplace("12.345", "00000.000", true);
		Assert.assertEquals(result, "00012.345");
	}

	@Test
	public void testMatch() {
		String result = StringUtils.match(ExpressionEnum.PACKAGE_NAME.getExpression(), "package org.pizazz2.common;", 1);
		Assert.assertEquals(result, "org.pizazz2.common");
	}

	@Test
	public void testCapitalize() {
		String result = StringUtils.capitalize("testCapitalize");
		Assert.assertEquals(result, "TestCapitalize");
	}
}

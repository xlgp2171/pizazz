package org.pizazz2.common;

import org.junit.Test;
import org.pizazz2.message.ExpressionEnum;

/**
 * ValidateUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ValidateUtilsTest {

	@Test
	public void testVerifyExpression() {
		ValidateUtils.verifyExpression(ExpressionEnum.CN_OR_EN, "中文ABC");
	}

	@Test
	public void testNotNull() {
		ValidateUtils.notNull("testNotNull", 1, true, "");
	}
}

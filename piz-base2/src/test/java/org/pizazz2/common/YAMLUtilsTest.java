package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.TupleObjectHelper;

/**
 * YAMLUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class YAMLUtilsTest {

	@Test
	public void testFromYAML() throws UtilityException {
		TupleObject tmp = YAMLUtils.fromYAML("message.yml");
		boolean result = TupleObjectHelper.getNestedBoolean(tmp, false, "message", "param1");
		Assert.assertTrue(result);
	}
}

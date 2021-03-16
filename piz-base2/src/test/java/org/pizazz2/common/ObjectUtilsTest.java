package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * ObjectUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ObjectUtilsTest {

	@Test
	public void testGetObjectsLength() {
		long result = ObjectUtils.getObjectsLength(12, true);
		Assert.assertEquals(result, 5, 0);
	}
}

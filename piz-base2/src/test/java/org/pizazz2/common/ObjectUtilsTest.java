package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * ObjectUtils测试
 *
 * @author xlgp2171
 * @version 2.2.230323
 */
public class ObjectUtilsTest {

	@Test
	public void testConvertPrimitive() {
		long result = ObjectUtils.convertPrimitive("23", Long.class);
		Assert.assertEquals(result, 23);
	}

	@Test
	public void testGetObjectsLength() {
		long result = ObjectUtils.getObjectsLength(12, true);
		Assert.assertEquals(result, 5, 0);
	}

	@Test
	public void testIsArray() {
		boolean result = ObjectUtils.isArray(new String[]{ "A", "B" });
		Assert.assertTrue(result);
	}
}

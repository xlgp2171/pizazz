package org.pizazz2.common;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

/**
 * ClassUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ClassUtilsTest {
	static String CODE_TXT;

	@BeforeClass
	public static void init() throws UtilityException, IOException {
		String resource = "DynamicObject.tmp";
		// 加载文件
		try (InputStream in = IOUtils.getResourceAsStream(resource)) {
			CODE_TXT = IOUtils.readInputStream(in);
		}
	}

	@Test
	public void testGetPackageName() {
		System.out.println(CODE_TXT);
		String result = ClassUtils.getPackageName(CODE_TXT);
		System.out.println(result);
		Assert.assertEquals(result, "org.pizazz2.test");
	}

	@Test
	public void testGetClassName() {
		String result = ClassUtils.getClassName(CODE_TXT);
		Assert.assertEquals(result, "DynamicObject");
	}
}

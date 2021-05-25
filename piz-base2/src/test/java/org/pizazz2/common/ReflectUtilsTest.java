package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.test.ParentObject;
import org.pizazz2.test.SubObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ReflectUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ReflectUtilsTest {

	@Test
	public void testInvokeMethod() throws UtilityException {
		String target = "中文";
		ParentObject parent = new ParentObject("P", 20.1, target.getBytes(StandardCharsets.UTF_8));
		String result = ReflectUtils.invokeMethod(parent, "ofCharset", new Class<?>[]{ Charset.class },
				new Object[]{ StandardCharsets.UTF_8 }, String.class, false);
		Assert.assertEquals(result,target);
	}

	@Test
	public void testInvokeSetField() throws UtilityException {
		double target = 30.1;
		ParentObject parent = new ParentObject("P", 20.1, "".getBytes());
		ReflectUtils.invokeSetField("parentSize", parent, target, true);
		Assert.assertEquals(parent.getParentSize(), target, 0);
	}

	@Test
	public void testGetFields() {
		Field[] fields = ReflectUtils.getFields(SubObject.class, true);
		Assert.assertEquals(fields[0].getName(), "subName");
	}

	@Test
	public void testGetAllFields() {
		Map<Class<?>, Field[]> result = ReflectUtils.getAllFields(SubObject.class, true);
		Field[] fields = result.get(ParentObject.class);
		Assert.assertEquals(fields[0].getName(), "parentName");
	}

	@Test
	public void testInvokeConstructor() throws UtilityException {
		double target = 30.1;
		ParentObject parent = ReflectUtils.invokeConstructor(ParentObject.class, new Class<?>[]{String.class, double.class,
				byte[].class}, new Object[]{"P", target, "中文".getBytes()}, true);
		Assert.assertEquals(parent.getParentSize(), target, 0);
	}

	@Test
	public void testGetPackageName() {
		String result = ReflectUtils.getPackageName(ParentObject.class);
		Assert.assertEquals("org.pizazz2.test", result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetActualTypeArguments() {
		// 实例化子类
		LinkedObject<byte[]> object1 = new LinkedObject<byte[]>(-1L, StringUtils.EMPTY, StringUtils.EMPTY){};
		Type type = ReflectUtils.getActualTypeArguments(object1.getClass())[0];
		Class<byte[]> clazz = (Class<byte[]>) type;
		Assert.assertEquals(type.getTypeName(), "byte[]");
		Assert.assertEquals(clazz, byte[].class);
	}
}

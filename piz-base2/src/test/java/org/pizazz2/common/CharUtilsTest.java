package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * CharUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CharUtilsTest {

	@Test
	public void testToUnicodeValueAndToChar() {
		char target = '中';
		String tmp = CharUtils.toUnicodeValue(target);
		char result = CharUtils.toChar(tmp);
		Assert.assertEquals(target, result);
	}

	@Test
	public void testFromUnicode() {
		char target1 = '中';
		char target2 = '文';
		String unicode1 = CharUtils.toUnicodeValue(target1);
		String unicode2 = CharUtils.toUnicodeValue(target2);
		String result = CharUtils.fromUnicode(unicode1, unicode2);
		Assert.assertEquals(result, StringUtils.of(target1) + target2);
	}
}

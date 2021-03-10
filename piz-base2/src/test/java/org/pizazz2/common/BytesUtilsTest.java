package org.pizazz2.common;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.test.SerializableObject;

/**
 * BytesUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class BytesUtilsTest {

	@Test
	public void testToBytesAndToInt() {
		int target = 14134;
		byte[] tmp = BytesUtils.toBytes(target);
		int result = BytesUtils.toInt(tmp);
		Assert.assertEquals(target, result);
	}

	@Test
	public void testBuffer() {
		String target1 = "AAAAAAAAAA";
		String target2 = "BBBBB";
		String target3 = "CCCCCCCCCCCCCCC";
		byte[] tmp = BytesUtils.buffer(target1.length(), target1.getBytes(),
				target2.length(), target2.getBytes(),
				target3.length(), target3.getBytes());
		int length = Integer.BYTES + target1.length() + Integer.BYTES;
		byte[] result = Arrays.copyOfRange(tmp, length, length + target2.length());
		Assert.assertEquals(target2, new String(result));
	}

	@Test
	public void testBuffer1() {
		String target = "中文字符";
		SerializableObject obj = new SerializableObject(false, 1.2, target);
		byte[] tmp = BytesUtils.buffer(obj);
		obj = new SerializableObject();
		obj.deserialize(tmp);
		Assert.assertEquals(target, obj.getText());
	}
}

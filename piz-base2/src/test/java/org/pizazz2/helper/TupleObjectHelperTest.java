package org.pizazz2.helper;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.TupleObject;

/**
 * TupleObjectHelper测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class TupleObjectHelperTest {

	@Test
	public void testNewObjectAndCopy() {
		double target = 23.4;
		TupleObject one = TupleObjectHelper.newObject("param1", "true").append("param3", target);
		TupleObject tmp = TupleObjectHelper.copy(one, "param3");
		double result = TupleObjectHelper.getDouble(tmp, "param3", 0.0);
		Assert.assertEquals(result, target, 0);
	}

	@Test
	public void testGetNestedString() {
		String target = "中文";
		TupleObject tmp = TupleObjectHelper.newObject("param4", target);
		TupleObject one = TupleObjectHelper.newObject("param1", "true").append("nested", tmp);
		String result = TupleObjectHelper.getNestedString(one, StringUtils.EMPTY,"nested", "param4");
		Assert.assertEquals(result, target );
	}

	@Test
	public void testGetStringArray() {
		TupleObject data = TupleObjectHelper.newObject(1);
		data.put("key", new String[]{"A", "B"});
		String[] tmp = TupleObjectHelper.getStringArray(data, "key", ",", new String[]{"C", "D"});
		Assert.assertEquals(Arrays.toString(tmp),  "[A, B]");
	}
}

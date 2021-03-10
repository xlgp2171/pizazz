package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.ref.IJacksonConfig;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.test.SerializableObject;

/**
 * JSONUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class JSONUtilsTest {

	@Test
	public void testToJSONAndFromJSON() throws UtilityException {
		String target = "中文";
		String json = JSONUtils.toJSON(new SerializableObject(false, 1.2, target));
		TupleObject tmp = JSONUtils.fromJSON(json, TupleObject.class);
		Assert.assertEquals(tmp.get("text", ""), target);
	}

	@Test
	public void testToJSONAndFromJSON1() throws UtilityException {
		String target = "中文";
		String json = JSONUtils.toJSON(new SerializableObject(false, 1.2, target), IJacksonConfig.EMPTY);
		TupleObject tmp = JSONUtils.fromJSON(json, TupleObject.class, IJacksonConfig.EMPTY);
		Assert.assertEquals(tmp.get("text", ""), target);
	}
}

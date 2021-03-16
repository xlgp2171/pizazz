package org.pizazz2.common;

import com.esotericsoftware.kryo.Kryo;
import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.ref.IKryoConfig;
import org.pizazz2.test.SubObject;

import java.nio.charset.StandardCharsets;

/**
 * SerializationUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class SerializationUtilsTest {

	@Test
	public void testSerializeAndDeserialize() {
		IKryoConfig config = new IKryoConfig() {
			@Override
			public void set(Kryo kryo) {
				kryo.register(SubObject.class);
				kryo.register(byte[].class);
			}
		};
		String target = "中文";
		SubObject obj = new SubObject();
		obj.setParentName("PARENT");
		obj.setSubData(target.getBytes(StandardCharsets.UTF_8));
		byte[] data = SerializationUtils.serialize(obj, config);
		SubObject result = SerializationUtils.deserialize(data, SubObject.class, config);
		Assert.assertEquals(target, new String(result.getSubData(), StandardCharsets.UTF_8));
	}
}

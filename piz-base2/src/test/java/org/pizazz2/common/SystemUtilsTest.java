package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.IObject;
import org.pizazz2.IPlugin;

import java.time.Duration;

/**
 * SystemUtils测试
 *
 * @author xlgp2171
 * @version 2.1.220727
 */
public class SystemUtilsTest {

	@Test
	public void testDestroy() {
		IPlugin<IObject> plugin = new IPlugin<IObject>() {
			@Override
			public void initialize(IObject config) {
			}

			@Override
			public void destroy(Duration timeout) {
				Assert.assertTrue(timeout.isZero());
			}
		};
		SystemUtils.destroy(plugin, Duration.ZERO);
	}

	@Test
	public void testGetProcessId() {
		int pid = SystemUtils.getProcessId();
		Assert.assertTrue(pid > 1_000);
	}

	@Test
	public void testUUID() {
		String uuid = SystemUtils.newUUID();
		System.out.println(uuid);
		Assert.assertEquals(uuid.length(), 36, 36);
	}
}

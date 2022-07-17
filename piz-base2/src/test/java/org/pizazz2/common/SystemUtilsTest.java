package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.IObject;
import org.pizazz2.IPlugin;
import org.pizazz2.exception.BaseException;

import java.time.Duration;

/**
 * SystemUtils测试
 *
 * @author xlgp2171
 * @version 2.1.211201
 */
public class SystemUtilsTest {

	@Test
	public void testDestroy() {
		IPlugin<IObject> plugin = new IPlugin<IObject>() {
			@Override
			public void initialize(IObject config) throws BaseException {
			}

			@Override
			public void destroy(Duration timeout) {
			}
		};
		SystemUtils.destroy(plugin, Duration.ZERO);
	}

	@Test
	public void testGetProcessId() {
		int pid = SystemUtils.getProcessId();
		Assert.assertTrue(pid > 0);
	}
}

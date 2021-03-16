package org.pizazz2.helper;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.common.BooleanUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.message.TypeEnum;

/**
 * ConfigureHelper测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ConfigureHelperTest {

	@BeforeClass
	public static void setUp() {
		ConfigureHelper.validate(TypeEnum.BASIC);
	}

	@Test
	public void testGetInt() {
		int result = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_SHELL_POOL_MAX", 0);
		Assert.assertEquals(result, 16, 0);
	}

	@Test
	public void testGetConfig() {
		System.setProperty("piz.shell.pool.max", "10");
		int result = ConfigureHelper.getConfig(TypeEnum.BASIC, "piz.shell.pool.max", "DEF_SHELL_POOL_MIN", 0);
		result += ConfigureHelper.getConfig(TypeEnum.BASIC, "piz.shell.pool.mix", "DEF_SHELL_POOL_MAX", 0);
		Assert.assertEquals(result, 26, 0);
	}
}

package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.net.InetAddress;

/**
 * NetworkUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class NetworkUtilsTest {

	@Test
	public void testNullToEmpty() throws UtilityException {
		InetAddress address= NetworkUtils.nullToEmpty("localhost");
		Assert.assertEquals(address.toString(), "localhost/127.0.0.1");
	}
}

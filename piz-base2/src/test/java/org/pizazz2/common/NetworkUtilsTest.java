package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.ref.NetworkEnum;
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

	@Test
	public void testGetMACAddressAndToMACString() throws UtilityException {
		InetAddress[] address = NetworkUtils.getAddressesByNetwork(NetworkEnum.Inet.INET4);
		byte[] mac = NetworkUtils.getMACAddress(address[0]);
		String result = NetworkUtils.toMACString(mac);
		System.out.println(result);
	}

	@Test
	public void testIpToLong() throws UtilityException {
		long ip = NetworkUtils.ipToLong("192.168.0.1");
		Assert.assertEquals(3232235521L, ip);
	}

	@Test
	public void testGetIpAddressTypeByIP4() {
		NetworkEnum.IpAddressType type = NetworkUtils.getIpAddressTypeByIP4("192.168.0.1");
		Assert.assertEquals(type, NetworkEnum.IpAddressType.C);
	}
}

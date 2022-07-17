package org.pizazz2.common;

import org.pizazz2.common.ref.NetworkEnum;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ExpressionEnum;
import org.pizazz2.message.TypeEnum;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 网络工具
 * 
 * @author xlgp2171
 * @version 2.1.211201
 */
public class NetworkUtils {
	public static final String IP_SEPARATOR = "\\.";
	public static final String LOCAL_HOST = "localhost";
	public static final String LOCAL_IP = "127.0.0.1";
	public static final int MAX_SYSTEM_PORT = 1024;

	public static InetAddress[] getAddressesByNetwork(NetworkEnum.Inet type) {
		Enumeration<NetworkInterface> network;
		try {
			network = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return new InetAddress[0];
		}
		if (!network.hasMoreElements()) {
			return new InetAddress[0];
		}
		Set<InetAddress> tmp = new LinkedHashSet<>();

		while (network.hasMoreElements()) {
			Enumeration<InetAddress> address = network.nextElement().getInetAddresses();

			while (address.hasMoreElements()) {
				InetAddress item = address.nextElement();

				if (item instanceof Inet4Address && type != NetworkEnum.Inet.INET6) {
					tmp.add(item);
				}
				if (item instanceof Inet6Address && type != NetworkEnum.Inet.INET4) {
					tmp.add(item);
				}
			}
		}
		return tmp.toArray(new InetAddress[0]);
	}

	public static InetAddress getAddressByNetwork(NetworkEnum.Inet type) throws UtilityException {
		InetAddress[] address = NetworkUtils.getAddressesByNetwork(type);

		if (ArrayUtils.isEmpty(address)) {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				try {
					return InetAddress.getByName(LOCAL_IP);
				} catch (UnknownHostException e1) {
					String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.NETWORK.FOUND", e.getMessage());
					throw new UtilityException(BasicCodeEnum.MSG_0027, msg, e);
				}
			}
		} else {
			return address[0];
		}
	}

	public static InetAddress nullToEmpty(String host) throws UtilityException {
		if (!StringUtils.isEmpty(host)) {
			try {
				return InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				// do nothing
			}
		}
		return NetworkUtils.getAddressByNetwork(NetworkEnum.Inet.ANY);
	}

	public static String[] getHostByNetwork(NetworkEnum.Inet type, String... filter) {
		InetAddress[] address = NetworkUtils.getAddressesByNetwork(type);
		Set<String> filterS = ArrayUtils.asSet(filter);
		Set<String> tmp = new LinkedHashSet<>();

		for (InetAddress item : address) {
			NetworkUtils.hostFilter(item, filterS, tmp);
		}
		return tmp.toArray(new String[NumberUtils.ZERO.intValue()]);
	}

	public static void hostFilter(InetAddress address, Set<String> filter, Set<String> hosts) {
		if (address != null) {
			String host = address.getHostAddress();

			if (filter == null || !filter.contains(host)) {
				hosts.add(host);
			}
		}
	}

	public static String toURLString(String protocol, String host, int port, String path) {
		if (protocol == null) {
			protocol = StringUtils.EMPTY;
		}
		try {
			return new URL(protocol, host, port, path).toExternalForm();
		} catch (MalformedURLException e) {
			// do nothing
		}
		StringBuilder tmp = new StringBuilder(protocol).append(host).append(":").append(port);

		if (!StringUtils.isEmpty(path)) {
			if (!path.startsWith("/")) {
				tmp.append("/");
			}
			tmp.append(path);
		}
		return tmp.toString();
	}

	public static boolean isPortFree(int port, NetworkEnum.Socket type) {
		return type != null && type.isPortFree(port);
	}

	public static byte[] getMACAddress(InetAddress address) throws ValidateException, UtilityException {
		ValidateUtils.notNull("getMACAddress", address);
		// 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
		try {
			return NetworkInterface.getByInetAddress(address).getHardwareAddress();
		} catch (SocketException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.NETWORK", address.toString(),
					e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0016, msg, e);
		}
	}

	public static String toMACString(byte[] mac) {
		if (ArrayUtils.isEmpty(mac)) {
			return StringUtils.EMPTY;
		}
		// 下面代码是把mac地址拼装成String
		/*
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				builder.append("-");
			}
			// mac[i] & 0xFF 是为了把byte转化为正整数
			String tmp = Integer.toHexString(mac[i] & 0xFF);
			builder.append(tmp.length() == 1 ? 0 + tmp : tmp);
		}
		// 把字符串所有小写字母改为大写成为正规的mac地址并返回
		return builder.toString();
		 */
		return StringUtils.toHexString(mac,"-");
	}

	public static int ipToInt(String ip) throws ValidateException, UtilityException {
		ValidateUtils.notNull("ipToInt", ip);
		InetAddress address;
		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.NETWORK", ip, e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0016, msg, e);
		}
		// 获取网络字节顺序
		byte[] tmp = address.getAddress();
		return (NetworkUtils.toInt(tmp[0]) << 24) | (NetworkUtils.toInt(tmp[1]) << 16) |
				(NetworkUtils.toInt(tmp[2]) << 8) | NetworkUtils.toInt(tmp[3]);
	}

	public static long ipToLong(String target) throws ValidateException, UtilityException {
		return NetworkUtils.toLong(NetworkUtils.ipToInt(target));
	}

	public static String longToIp(long target) {
		Long[] tmp = new Long[] { (target >> 24) & 0xff, (target >> 16) & 0xff, (target >> 8) & 0xff, target & 0xff };
		String[] result = new String[tmp.length];

		for (int i = 0; i < tmp.length; i++) {
			result[i] = Integer.toString(tmp[i].intValue());
		}
		return StringUtils.join(result, ".");
	}

	static int toInt(byte target) {
		int result = target & 0x07f;
		return target < 0 ? result | 0x80 : result;
	}

	static long toLong(int target) {
		long result = target & 0x7fffffffL;
		return target < 0 ? result | 0x080000000L : result;
	}

	/**
	 * 从IP地址获取IP地址类型
	 * <li>1-126 A类
	 * <li>128-191 B类
	 * <li>192.223 C类
	 * <li>224-239 D类
	 * <li>240-254 E类
	 * 
	 * @param ip ip地址,以"."分割
	 * @return IP地址类型
	 * @throws ValidateException 参数为空异常，IP地址不匹配异常
	 */
	public static NetworkEnum.IpAddressType getIpAddressTypeByIp4(String ip) throws ValidateException {
		ValidateUtils.notNull("getIpAddressTypeByIP4", ip);
		ValidateUtils.verifyExpression(ExpressionEnum.IP_ADDRESS, ip);
		String[] ipArray = ip.split(NetworkUtils.IP_SEPARATOR);
		int netAddress = NumberUtils.toInt(ipArray[0], NumberUtils.ZERO.intValue());

		if (netAddress >= 1 && netAddress <= 126) {
			return NetworkEnum.IpAddressType.A;
		} else if (netAddress >= 128 && netAddress <= 191) {
			return NetworkEnum.IpAddressType.B;
		} else if (netAddress >= 192 && netAddress <= 223) {
			return NetworkEnum.IpAddressType.C;
		} else if (netAddress >= 224 && netAddress <= 239) {
			return NetworkEnum.IpAddressType.D;
		} else if (netAddress >= 240 && netAddress <= 254) {
			return NetworkEnum.IpAddressType.E;
		} else {
			return NetworkEnum.IpAddressType.UNKNOWN;
		}
	}
}

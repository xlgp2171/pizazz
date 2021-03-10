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
 * @version 2.0.210201
 */
public class NetworkUtils {
	public static String IP_SEPARATOR = "\\.";

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
			Enumeration<InetAddress> _address = network.nextElement().getInetAddresses();

			while (_address.hasMoreElements()) {
				InetAddress item = _address.nextElement();

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
		InetAddress[] _address = NetworkUtils.getAddressesByNetwork(type);

		if (ArrayUtils.isEmpty(_address)) {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				try {
					return InetAddress.getByName(NetworkEnum.LOCAL_IP);
				} catch (UnknownHostException e1) {
					String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.NETWORK.FOUND", e.getMessage());
					throw new UtilityException(BasicCodeEnum.MSG_0027, msg, e);
				}
			}
		} else {
			return _address[0];
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
		InetAddress[] _address = NetworkUtils.getAddressesByNetwork(type);
		Set<String> _filter = CollectionUtils.asHashSet(filter);
		Set<String> tmp = new LinkedHashSet<>();

		for (InetAddress item : _address) {
			NetworkUtils.hostFilter(item, _filter, tmp);
		}
		return tmp.toArray(new String[0]);
	}

	public static void hostFilter(InetAddress address, Set<String> filter, Set<String> hosts) {
		if (address != null) {
			String _host = address.getHostAddress();

			if (filter == null || !filter.contains(_host)) {
				hosts.add(_host);
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
		StringBuilder _builder = new StringBuilder();

		for (int _i = 0; _i < mac.length; _i++) {
			if (_i != 0) {
				_builder.append("-");
			}
			// mac[i] & 0xFF 是为了把byte转化为正整数
			String tmp = Integer.toHexString(mac[_i] & 0xFF);
			_builder.append(tmp.length() == 1 ? 0 + tmp : tmp);
		}
		// 把字符串所有小写字母改为大写成为正规的mac地址并返回
		return _builder.toString();
	}

	public static int ipToInt(String ip) throws ValidateException, UtilityException {
		ValidateUtils.notNull("ipToInt", ip);
		InetAddress _address;
		try {
			_address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.NETWORK", ip, e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0016, msg, e);
		}
		// 获取网络字节顺序
		byte[] tmp = _address.getAddress();
		return (NetworkUtils.toInt(tmp[0]) << 24) | (NetworkUtils.toInt(tmp[1]) << 16) |
				(NetworkUtils.toInt(tmp[2]) << 8) | NetworkUtils.toInt(tmp[3]);
	}

	public static long ipToLong(String target) throws ValidateException, UtilityException {
		return NetworkUtils.toLong(NetworkUtils.ipToInt(target));
	}

	public static String longToIp(long target) {
		Long[] tmp = new Long[] { (target >> 24) & 0xff, (target >> 16) & 0xff, (target >> 8) & 0xff, target & 0xff };
		String[] _result = new String[tmp.length];

		for (int _i = 0; _i < tmp.length; _i++) {
			_result[_i] = Integer.toString(tmp[_i].intValue());
		}
		return StringUtils.join(_result, ".");
	}

	static int toInt(byte target) {
		int _result = target & 0x07f;
		return target < 0 ? _result | 0x80 : _result;
	}

	static long toLong(int target) {
		long _result = target & 0x7fffffffL;
		return target < 0 ? _result | 0x080000000L : _result;
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
	public static NetworkEnum.IpAddressType getIpAddressTypeByIP4(String ip) throws ValidateException {
		ValidateUtils.notNull("getIpAddressTypeByIP4", ip);
		ValidateUtils.verifyExpression(ExpressionEnum.IP_ADDRESS, ip);
		String[] _ip = ip.split(NetworkUtils.IP_SEPARATOR);
		int _netAddress = NumberUtils.toInt(_ip[0], 0);

		if (_netAddress >= 1 && _netAddress <= 126) {
			return NetworkEnum.IpAddressType.A;
		} else if (_netAddress >= 128 && _netAddress <= 191) {
			return NetworkEnum.IpAddressType.B;
		} else if (_netAddress >= 192 && _netAddress <= 223) {
			return NetworkEnum.IpAddressType.C;
		} else if (_netAddress >= 224 && _netAddress <= 239) {
			return NetworkEnum.IpAddressType.D;
		} else if (_netAddress >= 240 && _netAddress <= 254) {
			return NetworkEnum.IpAddressType.E;
		} else {
			return NetworkEnum.IpAddressType.UNKNOWN;
		}
	}
}

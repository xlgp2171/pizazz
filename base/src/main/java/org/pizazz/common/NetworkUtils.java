package org.pizazz.common;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pizazz.common.ref.NetworkEnum;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 网络工具
 * 
 * @author xlgp2171
 * @version 1.1.190219
 */
public class NetworkUtils {

	public static InetAddress[] getAddressByNetwork(NetworkEnum.Inet type) {
		Enumeration<NetworkInterface> _network;
		try {
			_network = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return new InetAddress[0];
		}
		if (!_network.hasMoreElements()) {
			return new InetAddress[0];
		}
		Set<InetAddress> _tmp = new LinkedHashSet<InetAddress>();

		while (_network.hasMoreElements()) {
			Enumeration<InetAddress> _address = _network.nextElement().getInetAddresses();

			while (_address.hasMoreElements()) {
				InetAddress _item = _address.nextElement();

				if (_item instanceof Inet4Address && type != NetworkEnum.Inet.INET6) {
					_tmp.add(_item);
				}
				if (_item instanceof Inet6Address && type != NetworkEnum.Inet.INET4) {
					_tmp.add(_item);
				}
			}
		}
		return _tmp.toArray(new InetAddress[0]);
	}

	public static String[] getHostByNetwork(NetworkEnum.Inet type, String... filter) {
		InetAddress[] _address = getAddressByNetwork(type);
		Set<String> _filter = CollectionUtils.asHashSet(filter);
		Set<String> _tmp = new LinkedHashSet<String>();

		for (InetAddress _item : _address) {
			hostFilter(_item, _filter, _tmp);
		}
		return _tmp.toArray(new String[0]);
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
		}
		StringBuilder _tmp = new StringBuilder(protocol).append(host).append(":").append(port);

		if (!StringUtils.isEmpty(path)) {
			if (!path.startsWith("/")) {
				_tmp.append("/");
			}
			_tmp.append(path);
		}
		return _tmp.toString();
	}

	public static boolean isPortFree(int port, NetworkEnum.Socket type) {
		if (type == null) {
			return false;
		}
		return type.isPortFree(port);
	}

	public static byte[] getMACAddress(InetAddress address) throws AssertException, UtilityException {
		AssertUtils.assertNotNull("getMACAddress", address);
		// 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
		try {
			return NetworkInterface.getByInetAddress(address).getHardwareAddress();
		} catch (SocketException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.NETWORK", address.toString(),
					e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0016, _msg, e);
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
			String _tmp = Integer.toHexString(mac[_i] & 0xFF);
			_builder.append(_tmp.length() == 1 ? 0 + _tmp : _tmp);
		}
		// 把字符串所有小写字母改为大写成为正规的mac地址并返回
		return _builder.toString();
	}

	public static int ip2Int(String ip) throws AssertException, UtilityException {
		AssertUtils.assertNotNull("ip2Int", ip);
		InetAddress _address;
		try {
			_address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.NETWORK", ip, e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0016, _msg, e);
		}
		// 获取网络字节顺序
		byte[] _tmp = _address.getAddress();
		int result = (toInt(_tmp[0]) << 24) | (toInt(_tmp[1]) << 16) | (toInt(_tmp[2]) << 8) | toInt(_tmp[3]);
		return result;
	}

	public static long ip2Long(String target) throws AssertException, UtilityException {
		return toLong(ip2Int(target));
	}

	public static String long2ip(long target) {
		Long[] _tmp = new Long[] { (target >> 24) & 0xff, (target >> 16) & 0xff, (target >> 8) & 0xff, target & 0xff };
		String[] _result = new String[_tmp.length];

		for (int _i = 0; _i < _tmp.length; _i++) {
			_result[_i] = Integer.toString(_tmp[_i].intValue());
		}
		return StringUtils.join(_result, ".");
	}

	static int toInt(byte target) {
		int _result = target & 0x07f;
		return target < 0 ? _result |= 0x80 : _result;
	}

	static long toLong(int target) {
		long _result = target & 0x7fffffffL;
		return target < 0 ? _result |= 0x080000000L : _result;
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
	 * @return
	 * @throws BaseException
	 */
	public static NetworkEnum.IpAddressType getIpAddressTypeByIP4(String ip) throws AssertException {
		AssertUtils.assertNotNull("getIpAddressTypeByIP4", ip);
		String[] _ip = ip.split("\\.");
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

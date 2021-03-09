package org.pizazz2.common.ref;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.net.ServerSocketFactory;

/**
 * 网络处理枚举
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class NetworkEnum {
	public static final String LOCAL_HOST = "localhost";
	public static final String LOCAL_IP = "127.0.0.1";

	/**
	 * TCP或UDP<br>
	 * Spring代码参考
	 */
	public static enum Socket {
		/**
		 * TCP
		 */
		TCP {
			@Override
			public boolean isPortFree(int port) {
				try {
					ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName(LOCAL_HOST))
							.close();
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		},
		/**
		 * UDP
		 */
		UDP {
			@Override
			public boolean isPortFree(int port) {
				try {
					new DatagramSocket(port, InetAddress.getByName(LOCAL_HOST)).close();
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		},
		/**
		 * 任意
		 */
		ANY {
			@Override
			public boolean isPortFree(int port) {
				try {
					new java.net.Socket(LOCAL_IP, port).close();
					return true;
				} catch (IOException e) {
					return false;
				}
			}

		};
		public abstract boolean isPortFree(int port);
	}

	/**
	 * IP4或IP6
	 */
	public static enum Inet {
		/**
		 * IP4
		 */
		INET4,
		/**
		 * IP6
		 */
		INET6,
		/**
		 * 任意
		 */
		ANY;
	}

	/**
	 * IP地址类型
	 */
	public static enum IpAddressType {
		/**
		 * 1-126 A类
		 */
		A,
		/**
		 * 128-191 B类
		 */
		B,
		/**
		 * 192.223 C类
		 */
		C,
		/**
		 * 224-239 D类
		 */
		D,
		/**
		 * 240-254 E类
		 */
		E,
		/**
		 * 未知类型
		 */
		UNKNOWN;
	}
}

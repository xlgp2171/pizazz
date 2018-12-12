package org.pizazz.common.ref;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.net.ServerSocketFactory;

/**
 * 网络处理枚举
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class NetworkEnum {
	public static final String LOCAL_HOST = "localhost";
	public static final String LOCAL_IP = "127.0.0.1";

	/**
	 * TCP或UDP<br>
	 * Spring代码参考
	 */
	public static enum Socket {
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
		INET4, INET6, ANY;
	}

	/**
	 * IP地址类型
	 */
	public static enum IpAddressType {
		A, B, C, D, E, UNKNOWN;
	}
}

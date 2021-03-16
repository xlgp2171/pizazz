package org.pizazz2.tool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * SocketContainer测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class SocketContainerTest {
	public void destroyContainerExample() throws IOException {
		String ip = "127.0.0.1";
		int port = 14134;
		String key = "pizazz";

		try (DatagramSocket socket = new DatagramSocket()) {
			DatagramPacket packet = new DatagramPacket(key.getBytes(), key.length(), InetAddress.getByName(ip), port);
			socket.send(packet);
		}
	}
}

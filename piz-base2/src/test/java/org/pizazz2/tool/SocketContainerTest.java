package org.pizazz2.tool;

import org.junit.Test;
import org.pizazz2.IMessageOutput;
import org.pizazz2.common.NetworkUtils;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.common.ref.NetworkEnum;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.test.BusinessProcessor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * SocketContainer测试
 *
 * @author xlgp2171
 * @version 2.0.211028
 */
public class SocketContainerTest {
	static final int PORT = 14134;
	static final String KEY = "xlgp2171";

	static final TupleObject CONFIG = TupleObjectHelper.newObject(SocketContainer.CONTAINER_HOST, NetworkUtils.LOCAL_IP)
			.append(SocketContainer.CONTAINER_PORT, PORT).append(SocketContainer.CONTAINER_KEY, KEY);

	@Test
	public void testNewContainer() throws BaseException {
		ThreadUtils.executeThread(() -> {
			try {
				Thread.sleep(1000);
				testDestroyContainer();
			} catch (Exception e) {
				// do nothing
			}
		});
		SocketContainer.run(new BusinessProcessor(false, false), CONFIG);
	}

	@Test
	public void testDestroyContainer() throws ToolException {
		SocketContainer.destroyContainer(CONFIG);
	}
}

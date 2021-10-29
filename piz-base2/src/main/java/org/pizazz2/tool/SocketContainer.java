package org.pizazz2.tool;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.pizazz2.*;
import org.pizazz2.common.*;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.ConfigureHelper;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 维持容器组件
 *
 * @author xlgp2171
 * @version 2.1.211028
 */
public class SocketContainer extends AbstractContainer<String> {
	public static final String CONTAINER_HOST = "host";
	public static final String CONTAINER_PORT = "port";
	public static final String CONTAINER_KEY = "key";
	public static final String COMMAND_LENGTH = "cmd.len";

	private final AtomicBoolean closed = new AtomicBoolean(false);
	private DatagramSocket socket;
	private Thread hook;

	/**
	 *
	 * @param runnable 容器销毁时需要安全关闭的组件
	 * @param config 容器配置
	 * @param output 容器日志输出接口
	 * @throws ValidateException 验证异常
	 */
	public SocketContainer(IRunnable runnable, TupleObject config, IMessageOutput<String> output)
			throws ValidateException {
		super(runnable, output);
		try {
			initialize(config);
		} catch (ToolException e) {
			super.output.throwException(e);
		}
	}

	public static void run(IRunnable runnable, TupleObject config) throws BaseException {
		SocketContainer.run(runnable, config, null);
	}

	public static void run(IRunnable runnable, TupleObject config, IMessageOutput<String> output) throws BaseException {
		new SocketContainer(runnable, TupleObjectHelper.nullToEmpty(config),
				output == null ? IMessageOutput.EMPTY_STRING : output).activate().waitForShutdown();
	}

	private SocketContainer setProperty(IObject config, String key, String defValue) {
		String value = ResourceUtils.getProperty(config, key, PizContext.NAMING_SHORT + ".sc." + key, defValue);
		super.properties.append(PizContext.ATTRIBUTE_PREFIX + key, value);
		return this;
	}

	private SocketContainer setProperty(IObject config, String key, String configKey, int defValue) {
		Object value = config.get(key, StringUtils.EMPTY);

		if (value == null || NumberUtils.toInt(StringUtils.of(value), NumberUtils.NEGATIVE_ONE.intValue()) ==
				NumberUtils.NEGATIVE_ONE.intValue()) {
			value = ConfigureHelper.getConfig(TypeEnum.BASIC,
					PizContext.NAMING_SHORT + ".sc." + key, configKey, defValue);
		}
		super.properties.append(PizContext.ATTRIBUTE_PREFIX + key, value);
		return this;
	}

	@Override
	public void initialize(IObject config) throws ToolException {
		super.initialize(config);
		// -Dpiz.sc.host
		setProperty(config, CONTAINER_HOST, StringUtils.EMPTY)
		// -Dpiz.sc.port
		.setProperty(config, CONTAINER_PORT, "DEF_CONTAINER_PORT", NumberUtils.NEGATIVE_ONE.intValue())
		// －Dpiz.sc.key
		.setProperty(config, CONTAINER_KEY, SystemUtils.newUUIDSimple())
		// -Dpiz.sc.cmd.len
		.setProperty(config, COMMAND_LENGTH, "DEF_COMMAND_LENGTH", NumberUtils.ZERO.intValue());
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	public SocketContainer activate() {
		super.runnable.run();
		return this;
	}

	@Override
	public void waitForShutdown() throws BaseException {
		if (!closed.get()) {
			hook = SystemUtils.addShutdownHook(this, null);
			int port = TupleObjectHelper.getInt(super.properties,
					PizContext.ATTRIBUTE_PREFIX + CONTAINER_PORT, NumberUtils.NEGATIVE_ONE.intValue());
			// port为0也可以super.waitForShutdown
			if (port <= 1_000) {
				if (super.output.isEnabled()) {
					super.output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
				}
				super.waitForShutdown();
			} else {
				socketWaiting(port);
				SystemUtils.destroy(this, Duration.ofMillis(NumberUtils.NEGATIVE_ONE.intValue()));
			}
		}
	}

	protected void socketWaiting(int port) throws UtilityException, ToolException {
		// 终止socket的字符串
		String key = TupleObjectHelper.getString(super.properties, PizContext.ATTRIBUTE_PREFIX + CONTAINER_KEY,
				StringUtils.EMPTY);
		key = StringUtils.isEmpty(key) ? PizContext.NAMING : key;
		// 字符串数据长度，根据字符串长度和设置长度定义
		int lenMax = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_COMMAND_LENGTH_MAX", 1024);
		int cmdLen = TupleObjectHelper.getInt(
				super.properties, PizContext.ATTRIBUTE_PREFIX + COMMAND_LENGTH, key.length());
		cmdLen = (cmdLen < key.length() || cmdLen > lenMax) ? key.length() : cmdLen;
		// 获取配置host
		String host = TupleObjectHelper.getString(super.properties, PizContext.ATTRIBUTE_PREFIX + CONTAINER_HOST,
				StringUtils.EMPTY);
		InetAddress address = NetworkUtils.nullToEmpty(host);
		try {
			socket = new DatagramSocket(port, address);
		} catch (SocketException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET", e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0016, msg, e);
		}
		// FIXME xlgp2171:若需要DatagramPacket工具，将此处代码移出
		byte[] data = new byte[cmdLen];
		DatagramPacket packet = new DatagramPacket(data, data.length);

		if (super.output.isEnabled()) {
			super.output.write(LocaleHelper.toLocaleText(
					TypeEnum.BASIC, "CONTAINER.HOST", address.getHostAddress()));
			super.output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.PORT", StringUtils.of(port)));
			super.output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.KEY", key));
			super.output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
		}
		while (true) {
			try {
				socket.receive(packet);
			} catch (IOException e) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.RECEIVE",
						address.getHostAddress() + ":" + port, e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0016, msg);
			}
			String cmd = new String(packet.getData(), PizContext.LOCAL_ENCODING).trim();
			boolean valid = key.equals(cmd);

			if (super.output.isEnabled()) {
				super.output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.VALID", cmd, valid));
			}
			if (valid) {
				break;
			} else {
				try {
					commandProcess(cmd);
				} catch (Exception e) {
					super.output.throwException(e);
				}
			}
			packet.setData(new byte[cmdLen]);
		}
	}

	protected void commandProcess(String cmd) {
	}

	@Override
	protected void log(String msg, Exception e) {
		if (super.output.isEnabled() || e != null) {
			super.output.write(msg);
		}
	}

	@Override
	public void destroy(Duration timeout) {
		if (closed.compareAndSet(false, true)) {
			if (hook != null) {
				Runtime.getRuntime().removeShutdownHook(hook);
			}
			SystemUtils.close(socket);
			super.destroy(timeout);
		}
	}

	/**
	 * 容器关闭方法
	 * @param config 参数
	 * @throws ToolException 关闭消息发送异常
	 */
	public static void destroyContainer(IObject config) throws ToolException {
		// 获取host
		String host = ResourceUtils.getProperty(config, CONTAINER_HOST,
				PizContext.NAMING_SHORT + ".sc." + CONTAINER_HOST, NetworkUtils.LOCAL_IP);
		// 获取port
		String portString = ResourceUtils.getProperty(config, CONTAINER_PORT,
				PizContext.NAMING_SHORT + ".sc." + CONTAINER_PORT, StringUtils.EMPTY);
		int port;

		if (StringUtils.isEmpty(portString)) {
			port = ConfigureHelper.getInt(
					TypeEnum.BASIC, "DEF_CONTAINER_PORT", NumberUtils.NEGATIVE_ONE.intValue());
		} else {
			port = NumberUtils.toInt(portString, NumberUtils.NEGATIVE_ONE.intValue());
		}
		// 获取key
		String key = ResourceUtils.getProperty(config, CONTAINER_KEY,
				PizContext.NAMING_SHORT + ".sc." + CONTAINER_KEY, PizContext.NAMING);
		// 发送关闭消息
		try (DatagramSocket socket = new DatagramSocket()) {
			DatagramPacket packet = new DatagramPacket(key.getBytes(), key.length(), InetAddress.getByName(host), port);
			socket.send(packet);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.SENT",
					host + ":" + port, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0016, msg);
		}
	}
}

package org.pizazz2.tool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz2.PizContext;
import org.pizazz2.IMessageOutput;
import org.pizazz2.IObject;
import org.pizazz2.IPlugin;
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
 * @version 2.0.210201
 */
public class SocketContainer extends AbstractContainer<String> {
	public static final String CONTAINER_HOST = "host";
	public static final String CONTAINER_PORT = "port";
	public static final String CONTAINER_KEY = "key";
	public static final String COMMAND_LENGTH = "cmd.len";

	private final AtomicBoolean closed = new AtomicBoolean(false);
	private DatagramSocket socket;
	private Thread hook;
	private IMessageOutput<byte[]> command;

	/**
	 *
	 * @param plugin 容器销毁时需要安全关闭的组件
	 * @param config 容器配置
	 * @param output 容器日志输出接口
	 * @throws ValidateException 验证异常
	 */
	public SocketContainer(IPlugin plugin, TupleObject config, IMessageOutput<String> output) throws ValidateException {
		super(plugin, output);
		try {
			initialize(config);
		} catch (ToolException e) {
			output.throwException(e);
		}
	}

	private SocketContainer setProperty(IObject config, String key, String defValue) {
		Object value = config.get(key, StringUtils.EMPTY);

		if (value == null || StringUtils.isTrimEmpty(StringUtils.of(value))) {
			value = SystemUtils.getSystemProperty(PizContext.NAMING_SHORT + ".sc." + key, defValue);
		}
		properties.append(PizContext.ATTRIBUTE_PREFIX + key, value);
		return this;
	}

	private SocketContainer setProperty(IObject config, String key, String configKey, int defValue) {
		Object value = config.get(key, StringUtils.EMPTY);

		if (value == null || NumberUtils.toInt(StringUtils.of(value), -1) == -1) {
			value = ConfigureHelper.getConfig(TypeEnum.BASIC,
					PizContext.NAMING_SHORT + ".sc." + key, configKey, defValue);
		}
		properties.append(PizContext.ATTRIBUTE_PREFIX + key, value);
		return this;
	}

	@Override
	public void initialize(IObject config) throws ToolException {
		super.initialize(config);
		// -Dpiz.sc.host
		setProperty(config, CONTAINER_HOST, StringUtils.EMPTY)
		// -Dpiz.sc.port
		.setProperty(config, CONTAINER_PORT, "DEF_CONTAINER_PORT",-1)
		// －Dpiz.sc.key
		.setProperty(config, CONTAINER_KEY, SystemUtils.newUUIDSimple())
		// -Dpiz.sc.cmd.len
		.setProperty(config, COMMAND_LENGTH, "DEF_COMMAND_LENGTH",0);
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public void waitForShutdown() throws BaseException {
		hook = SystemUtils.addShutdownHook(this, null);
		int port = TupleObjectHelper.getInt(properties, PizContext.ATTRIBUTE_PREFIX + CONTAINER_PORT, -1);
		// port为0也可以super.waitForShutdown
		if (port <= 1_000) {
			if (output.isEnabled()) {
				output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
			}
			super.waitForShutdown();
		} else {
			socketWaiting(port);
			SystemUtils.destroy(this, Duration.ofMillis(-1));
		}
	}

	protected void socketWaiting(int port) throws UtilityException, ToolException {
		// 终止socket的字符串
		String key = TupleObjectHelper.getString(properties, PizContext.ATTRIBUTE_PREFIX + CONTAINER_KEY,
				StringUtils.EMPTY);
		key = StringUtils.isEmpty(key) ? PizContext.NAMING : key;
		// 字符串数据长度，根据字符串长度和设置长度定义
		int lenMax = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_COMMAND_LENGTH_MAX", 1024);
		int cmdLen = TupleObjectHelper.getInt(properties, PizContext.ATTRIBUTE_PREFIX + COMMAND_LENGTH, key.length());
		cmdLen = (cmdLen < key.length() || cmdLen > lenMax) ? key.length() : cmdLen;
		// 获取配置host
		String host = TupleObjectHelper.getString(properties, PizContext.ATTRIBUTE_PREFIX + CONTAINER_HOST,
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

		if (output.isEnabled()) {
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.HOST", address.getHostAddress()));
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.PORT", StringUtils.of(port)));
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.KEY", key));
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
		}
		while (true) {
			try {
				socket.receive(packet);
			} catch (IOException e) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.RECEIVE", StringUtils.of(port),
						e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0016, msg);
			}
			data = command(packet.getData());
			String tmp = new String(data, PizContext.LOCAL_ENCODING).trim();
			boolean valid = key.equals(tmp);

			if (output.isEnabled()) {
				output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.VALID", tmp, valid));
			}
			if (valid) {
				break;
			}
			packet.setData(new byte[cmdLen]);
		}
	}

	protected byte[] command(byte[] data) {
		if (command != null) {
			try {
				command.write(data);
			} catch (Exception e) {
				output.throwException(e);
			}
		}
		return data;
	}

	public SocketContainer setCommand(IMessageOutput<byte[]> command) {
		this.command = command;
		return this;
	}

	@Override
	protected void log(String msg, Exception e) {
		if (output.isEnabled()) {
			output.write(msg);
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
}

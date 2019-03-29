package org.pizazz.tool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz.Constant;
import org.pizazz.IMessageOutput;
import org.pizazz.IPlugin;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.NetworkUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.common.ref.NetworkEnum;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.message.ErrorCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 维持容器组件
 *
 * @author xlgp2171
 * @version 1.6.190328
 */
public class SocketContainer extends AbstractContainer<String> {
	public static final String CONTAINER_HOST = "host";
	public static final String CONTAINER_PORT = "port";
	public static final String CONTAINER_KEY = "key";
	public static final String COMMAND_LENGTH = "cmd.len";

	private DatagramSocket socket;
	private Thread hook;
	private IMessageOutput<byte[]> command;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public SocketContainer(IPlugin plugin, TupleObject config, IMessageOutput<String> output) throws AssertException {
		super(plugin, output);
		try {
			initialize(config);
		} catch (BaseException e) {
			this.output_.throwException(e);
		}
	}

	@Override
	public void initialize(TupleObject config) throws ToolException {
		super.initialize(config);
		// -Dpiz.sc.host
		String _host = TupleObjectHelper.getString(config, CONTAINER_HOST, StringUtils.EMPTY);
		// -Dpiz.sc.port
		int _port = TupleObjectHelper.getInt(config, CONTAINER_PORT, -1);
		// －Dpiz.sc.key
		String _key = TupleObjectHelper.getString(config, CONTAINER_KEY, StringUtils.EMPTY);
		// -Dpiz.sc.cmd.len
		int _cmdLen = TupleObjectHelper.getInt(config, COMMAND_LENGTH, -1);
		// length和port的判断值需要判断==-1
		properties_
				.append(Constant.ATTRIBUTE_PREFIX + CONTAINER_HOST,
						StringUtils.isTrimEmpty(_host) ? SystemUtils.getSystemProperty(
								Constant.NAMING_SHORT + ".sc." + CONTAINER_HOST, StringUtils.EMPTY) : _host)
				.append(Constant.ATTRIBUTE_PREFIX + CONTAINER_PORT,
						_port == -1
								? ConfigureHelper.getConfig(TypeEnum.BASIC,
										Constant.NAMING_SHORT + ".sc." + CONTAINER_PORT, "DEF_CONTAINER_PORT", -1)
								: _port)
				.append(Constant.ATTRIBUTE_PREFIX + CONTAINER_KEY,
						StringUtils.isTrimEmpty(_key) ? ConfigureHelper.getConfig(TypeEnum.BASIC,
								Constant.NAMING_SHORT + ".sc." + CONTAINER_KEY, "DEF_CONTAINER_KEY",
								SystemUtils.newUUIDSimple()) : _key)
				.append(Constant.ATTRIBUTE_PREFIX + COMMAND_LENGTH,
						_cmdLen == -1
								? ConfigureHelper.getConfig(TypeEnum.BASIC,
										Constant.NAMING_SHORT + ".sc." + COMMAND_LENGTH, "DEF_COMMAND_LENGTH", -1)
								: _cmdLen);
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public void waitForShutdown() {
		hook = SystemUtils.addShutdownHook(this, null);
		int _port = TupleObjectHelper.getInt(properties_, Constant.ATTRIBUTE_PREFIX + CONTAINER_PORT, -1);
		// port为0也可以super.waitForShutdown
		if (_port <= 0) {
			if (output_.isEnable()) {
				output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
			}
			super.waitForShutdown();
		} else {
			socketWaiting(_port);
			SystemUtils.destroy(this, Duration.ofMillis(-1));
		}
	}

	protected void socketWaiting(int port) {
		String _key = TupleObjectHelper.getString(properties_, Constant.ATTRIBUTE_PREFIX + CONTAINER_KEY,
				StringUtils.EMPTY);
		_key = StringUtils.isEmpty(_key) ? Constant.NAMING : _key;
		int _lenMax = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_COMMAND_LENGTH_MAX", 1024);
		int _cmdLen = TupleObjectHelper.getInt(properties_, Constant.ATTRIBUTE_PREFIX + COMMAND_LENGTH, _key.length());
		_cmdLen = (_cmdLen < _key.length() || _cmdLen > _lenMax) ? _key.length() : _cmdLen;
		InetAddress _address = hostNotNull();
		try {
			socket = new DatagramSocket(port, _address);
		} catch (SocketException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET", e.getMessage());
			throw new BaseError(ErrorCodeEnum.ERR_0003, _msg, e);
		}
		// FIXME xlgp2171:若需要DatagramPacket工具，将此处代码移出
		byte[] _data = new byte[_cmdLen];
		DatagramPacket _packet = new DatagramPacket(_data, _data.length);

		if (output_.isEnable()) {
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.HOST", _address.getHostAddress()));
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.PORT", StringUtils.of(port)));
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.KEY", _key));
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.ALIVE"));
		}
		while (true) {
			try {
				socket.receive(_packet);
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.RECEIVE", StringUtils.of(port),
						e.getMessage());
				throw new BaseError(ErrorCodeEnum.ERR_0004, _msg);
			}
			_data = command(_packet.getData());
			String _tmp = new String(_data, SystemUtils.LOCAL_ENCODING).trim();
			boolean _valid = _key.equals(_tmp);

			if (output_.isEnable()) {
				output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.VALID", _tmp, _valid));
			}
			if (_valid) {
				break;
			}
			_packet.setData(new byte[_cmdLen]);
		}
	}

	private InetAddress hostNotNull() {
		String _host = TupleObjectHelper.getString(properties_, Constant.ATTRIBUTE_PREFIX + CONTAINER_HOST,
				StringUtils.EMPTY);

		if (StringUtils.isEmpty(_host)) {
			InetAddress[] _address = NetworkUtils.getAddressByNetwork(NetworkEnum.Inet.INET4);

			if (ArrayUtils.isEmpty(_address)) {
				_host = NetworkEnum.LOCAL_IP;
			} else {
				return _address[0];
			}
		}
		try {
			return InetAddress.getByName(_host);
		} catch (UnknownHostException e) {
			try {
				return InetAddress.getByName(NetworkEnum.LOCAL_IP);
			} catch (UnknownHostException e1) {
				throw new BaseError(ErrorCodeEnum.ERR_0006, e.getMessage(), e);
			}
		}
	}

	protected byte[] command(byte[] data) {
		if (command != null) {
			try {
				command.write(data);
			} catch (Exception e) {
				output_.throwException(e);
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
		if (output_.isEnable()) {
			output_.write(msg);
		}
	}

	@Override
	public void destroy(Duration timeout) {
		if (closed.compareAndSet(false, true)) {
			if (hook != null) {
				Runtime.getRuntime().removeShutdownHook(hook);
			}
			IOUtils.close(socket);
			super.destroy(timeout);
		}
	}
}

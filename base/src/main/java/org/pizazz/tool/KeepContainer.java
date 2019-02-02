package org.pizazz.tool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz.Constant;
import org.pizazz.IMessageOutput;
import org.pizazz.IPlugin;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.message.ErrorCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 维持容器组件
 *
 * @author xlgp2171
 * @version 1.3.190202
 */
public class KeepContainer extends AbstractContainer<String> {
	public static final String KEY_CONTAINER_PORT = "$PORT";
	public static final String KEY_CONTAINER_KEY = "$KEY";

	private DatagramSocket socket;
	private Thread hook;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public KeepContainer(IPlugin plugin, TupleObject config, IMessageOutput<String> output) throws BaseException {
		super(plugin, output);
		try {
			initialize(config);
		} catch (BaseException e) {
			this.output_.throwException(e);
		}
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
		super.initialize(config);
		int _port = TupleObjectHelper.getInt(config, "port", -1);
		String _key = TupleObjectHelper.getString(config, "key", StringUtils.EMPTY);
		properties_
				.append(KEY_CONTAINER_PORT,
						_port == -1 ? ConfigureHelper.getConfig(TypeEnum.BASIC, Constant.NAMING_SHORT + ".kc.port",
								"DEF_CONTAINER_PORT", -1) : _port)
				.append(KEY_CONTAINER_KEY,
						StringUtils.isTrimEmpty(_key) ? ConfigureHelper.getConfig(TypeEnum.BASIC,
								Constant.NAMING_SHORT + ".kc.key", "DEF_CONTAINER_KEY", SystemUtils.newUUIDSimple())
								: _key);
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public void waitForShutdown() {
		hook = SystemUtils.addShutdownHook(this, null);
		int _port = TupleObjectHelper.getInt(properties_, KEY_CONTAINER_PORT, -1);
		// port为0也可以super.waitForShutdown
		if (_port <= 0) {
			super.waitForShutdown();
		} else {
			socketWaiting(_port);
			SystemUtils.destroy(this, Duration.ofMillis(-1));
		}
	}

	protected void socketWaiting(int port) {
		String _key = TupleObjectHelper.getString(properties_, KEY_CONTAINER_KEY, Constant.NAMING);
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET", e.getMessage());
			throw new BaseError(ErrorCodeEnum.ERR_0003, _msg, e);
		}
		// FIXME xlgp2171:若需要DatagramPacket工具，将此处代码移出
		byte[] _data = new byte[36];
		DatagramPacket _packet = new DatagramPacket(_data, _data.length);

		if (output_.isEnable()) {
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.PORT", StringUtils.of(port)));
			output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.KEY", _key));
		}
		while (true) {
			try {
				socket.receive(_packet);
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.RECEIVE", StringUtils.of(port),
						e.getMessage());
				throw new BaseError(ErrorCodeEnum.ERR_0004, _msg);
			}
			String _tmp = new String(_packet.getData(), SystemUtils.LOCAL_ENCODING).trim();
			boolean _valid = _key.equals(_tmp);

			if (output_.isEnable()) {
				output_.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.VALID", _tmp, _valid));
			}
			if (_valid) {
				break;
			}
			_packet.setData(new byte[36]);
		}
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (closed.compareAndSet(false, true)) {
			if (hook != null) {
				Runtime.getRuntime().removeShutdownHook(hook);
			}
			IOUtils.close(socket);
			super.destroy(timeout);
		}
	}
}

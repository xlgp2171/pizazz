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
 * @version 1.2.190127
 */
public class KeepContainer extends AbstractContainer<String> {
	private DatagramSocket socket;
	private Thread hook;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public KeepContainer(IPlugin plugin, TupleObject config, IMessageOutput<String> output) throws BaseException {
		super(plugin, output);
		try {
			initialize(config);
		} catch (BaseException e) {
			this.output.throwException(e);
		}
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public void waitForShutdown() {
		hook = SystemUtils.addShutdownHook(this, null);
		int _port = TupleObjectHelper.getInt(properties, KEY_CONTAINER_PORT, 10420);
		String _key = TupleObjectHelper.getString(properties, KEY_CONTAINER_KEY, Constant.NAMING);
		try {
			socket = new DatagramSocket(_port);
		} catch (SocketException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET", e.getMessage());
			throw new BaseError(ErrorCodeEnum.ERR_0003, _msg, e);
		}
		// FIXME xlgp2171:若需要DatagramPacket工具，将此处代码移出
		byte[] _data = new byte[36];
		DatagramPacket _packet = new DatagramPacket(_data, _data.length);

		if (output.isEnable()) {
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.PORT", StringUtils.of(_port)));
			output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.KEY", _key));
		}
		while (true) {
			try {
				socket.receive(_packet);
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SOCKET.RECEIVE", StringUtils.of(_port),
						e.getMessage());
				throw new BaseError(ErrorCodeEnum.ERR_0004, _msg);
			}
			String _tmp = new String(_packet.getData(), SystemUtils.LOCAL_ENCODING).trim();
			boolean _valid = _key.equals(_tmp);

			if (output.isEnable()) {
				output.write(LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.VALID", _tmp, _valid));
			}
			if (_valid) {
				break;
			}
			_packet.setData(new byte[36]);
		}
		SystemUtils.destroy(this, Duration.ofMillis(-1));
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

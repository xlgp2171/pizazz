package org.pizazz.tool;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.tool.ref.IShellFactory;

/**
 * SHELL运行组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class PShellBuilder implements ICloseable {

	private IShellFactory factory;
	private final ProcessBuilder builder;
	private Charset charset = StandardCharsets.UTF_8;
	private int timeout = 0;
	private Process tmpProcess;

	public PShellBuilder(IShellFactory factory, String[] command) throws BaseException {
		AssertUtils.assertNotNull("PShellBuilder", factory);
		this.factory = factory;
		builder = new ProcessBuilder();
		command(command);
	}

	public PShellBuilder command(String[] command) {
		if (!ArrayUtils.isEmpty(command)) {
			builder.command(ArrayUtils.merge(SystemUtils.LOCAL_OS.getEnvironment(), command));
		}
		return this;
	}

	public PShellBuilder charset(Charset charset) {
		if (charset != null) {
			this.charset = charset;
		}
		return this;
	}

	/**
	 * 设置等待超时时间<br>
	 * 其中大于0为超时设置，等于0为永不超时，小于0为不等待
	 * @param timeout
	 * @return
	 */
	public PShellBuilder waitFor(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public Map<String, String> environment() {
		return builder.environment();
	}

	private synchronized void turn(Process process, boolean force) {
		if (tmpProcess != null && process.isAlive()) {
			if (force) {
				tmpProcess.destroyForcibly();
			} else {
				tmpProcess.destroy();
			}
		}
		tmpProcess = process;
	}

	public FutureResult<List<String>> execute(IMessageOutput<String> outputS, IMessageOutput<String> outputE)
			throws BaseException {
		turn(factory.newProcess(builder, timeout), true);
		final CountDownLatch _latch = new CountDownLatch(2);
		ICloseable _shutdown = new ICloseable() {
			@Override
			public void destroy(int timeout) throws BaseException {
				_latch.countDown();

				if (_latch.getCount() <= 0) {
					turn(null, false);
				}
			}
		};
		return new FutureResult<List<String>>(factory.submit(tmpProcess.getInputStream(), charset, _shutdown, outputS),
				factory.submit(tmpProcess.getErrorStream(), charset, _shutdown, outputE));
	}

	public Future<List<String>> execute(IMessageOutput<String> output) throws BaseException {
		builder.redirectErrorStream(true);
		turn(factory.newProcess(builder, timeout), true);
		return factory.submit(tmpProcess.getInputStream(), charset, new ICloseable() {

			@Override
			public void destroy(int timeout) throws BaseException {
				turn(null, false);
			}
		}, output);
	}

	@Override
	public void destroy(int timeout) throws BaseException {
		turn(null, true);
	}

	public static class FutureResult<T> {
		private final Future<T> inputStream;
		private final Future<T> errorStream;

		public FutureResult(Future<T> inputStream, Future<T> errorStream) {
			this.inputStream = inputStream;
			this.errorStream = errorStream;
		}

		public Future<T> getInputStream() {
			return inputStream;
		}

		public Future<T> getErrorStream() {
			return errorStream;
		}
	}
}

package org.pizazz.tool;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.IObject;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.tool.ref.IShellFactory;

/**
 * SHELL运行组件
 * 
 * @author xlgp2171
 * @version 1.2.190213
 */
public class PShellBuilder implements ICloseable, IObject {

	private IShellFactory factory;
	private final ProcessBuilder builder;
	private Charset charset = StandardCharsets.UTF_8;
	private Duration timeout = Duration.ZERO;
	private Process tmpProcess;
	private String id = "";

	public PShellBuilder(IShellFactory factory, String[] command) throws AssertException {
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

	public String getId() {
		return id;
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
	 * 
	 * @param timeout
	 * @return
	 */
	public PShellBuilder waitFor(Duration timeout) {
		if (timeout != null) {
			this.timeout = timeout;
		}
		return this;
	}

	public Map<String, String> environment() {
		return builder.environment();
	}

	private synchronized void turn(String id, Process process, boolean force) {
		if (!this.id.equals(id)) {
			return;
		}
		if (tmpProcess != null && tmpProcess.isAlive()) {
			if (force) {
				tmpProcess.destroyForcibly();
			} else {
				tmpProcess.destroy();
			}
		}
		this.id = SystemUtils.newUUIDSimple();
		tmpProcess = process;
	}

	public FutureResult<List<String>> execute(IMessageOutput<String> outputS, IMessageOutput<String> outputE)
			throws BaseException {
		builder.redirectErrorStream(false);
		turn(this.id, factory.newProcess(builder, timeout), true);
		FutureResult<List<String>> _result = new FutureResult<List<String>>(
				factory.apply(tmpProcess.getInputStream(), charset, outputS),
				factory.apply(tmpProcess.getErrorStream(), charset, outputE));
		final String _currentId = this.id;
		CompletableFuture.allOf(_result.getInputFuture(), _result.getErrorFuture())
				.whenCompleteAsync((v, e) -> turn(_currentId, null, false), factory.getExecutorService());
		return _result;
	}

	public CompletableFuture<List<String>> execute(IMessageOutput<String> output) throws BaseException {
		builder.redirectErrorStream(true);
		turn(this.id, factory.newProcess(builder, timeout), true);
		final String _currentId = this.id;
		return factory.apply(tmpProcess.getInputStream(), charset, output).thenApply(v -> {
			turn(_currentId, null, false);
			return v;
		});
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		turn(id, null, true);
		id = "";
	}

	public static class FutureResult<T> {
		private final CompletableFuture<T> input;
		private final CompletableFuture<T> error;

		public FutureResult(CompletableFuture<T> input, CompletableFuture<T> error) {
			this.input = input;
			this.error = error;
		}

		public CompletableFuture<T> getInputFuture() {
			return input;
		}

		public CompletableFuture<T> getErrorFuture() {
			return error;
		}
	}
}

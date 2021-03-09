package org.pizazz2.tool;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.pizazz2.ICloseable;
import org.pizazz2.IMessageOutput;
import org.pizazz2.IObject;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.BaseException;
import org.pizazz2.tool.ref.IShellFactory;

/**
 * SHELL运行组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PShellBuilder implements ICloseable, IObject {

	private final PIdBuilder idBuilder = PIdFactory.newInstance();
	private final AtomicLong id = new AtomicLong(-1L);
	private final IShellFactory factory;
	private final ProcessBuilder builder;
	private Charset charset = StandardCharsets.UTF_8;
	private Duration timeout = Duration.ZERO;
	private Process tmpProcess;

	public PShellBuilder(IShellFactory factory, String[] command) throws ValidateException {
		ValidateUtils.notNull("PShellBuilder", factory);
		this.factory = factory;
		builder = new ProcessBuilder();
		command(command);
	}

	public void command(String[] command) {
		if (!ArrayUtils.isEmpty(command)) {
			builder.command(ArrayUtils.merge(SystemUtils.LOCAL_OS.getEnvironment(), command));
		}
	}

	@Override
	public String getId() {
		return StringUtils.of(id);
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
	 * @param timeout 等待超时时间
	 * @return 当前对象
	 */
	public PShellBuilder waitFor(Duration timeout) {
		if (timeout != null) {
			this.timeout = timeout;
		}
		return this;
	}

	/**
	 * 获取子进程环境信息
	 * @return 环境信息
	 */
	public Map<String, String> environment() {
		return builder.environment();
	}

	/**
	 * 下一轮操作（结束缓存进程，将指定进程缓存）
	 * @param id 指定识别id
	 * @param process 指定进程
	 * @param force 若缓存进程存在是否强制结束
	 */
	private synchronized void turn(long id, Process process, boolean force) {
		if (this.id.get() != id) {
			return;
		}
		if (tmpProcess != null && tmpProcess.isAlive()) {
			if (force) {
				tmpProcess.destroyForcibly();
			} else {
				tmpProcess.destroy();
			}
		}
		this.id.set(idBuilder.generate());
		tmpProcess = process;
	}

	public FutureResult<List<String>> execute(IMessageOutput<String> outputS, IMessageOutput<String> outputE)
			throws BaseException {
		builder.redirectErrorStream(false);
		turn(this.id.get(), factory.newProcess(builder, timeout), true);
		FutureResult<List<String>> result = new FutureResult<>(
				factory.apply(tmpProcess.getInputStream(), charset, outputS),
				factory.apply(tmpProcess.getErrorStream(), charset, outputE));
		CompletableFuture.allOf(result.getInputFuture(), result.getErrorFuture())
				.whenCompleteAsync((v, e) -> turn(this.id.get(), null, false), factory.getThreadPool());
		return result;
	}

	public CompletableFuture<List<String>> execute(IMessageOutput<String> output) throws BaseException {
		builder.redirectErrorStream(true);
		turn(this.id.get(), factory.newProcess(builder, timeout), true);
		return factory.apply(tmpProcess.getInputStream(), charset, output).thenApply(v -> {
			turn(this.id.get(), null, false);
			return v;
		});
	}

	@Override
	public void destroy(Duration timeout) {
		turn(id.get(), null, true);
		id.lazySet(-1L);
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

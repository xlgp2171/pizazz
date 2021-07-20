package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.IMessageOutput;
import org.pizazz2.PizContext;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.tool.ref.IShellFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SHELL运行组件
 * 
 * @author xlgp2171
 * @version 2.1.210720
 */
public class ShellBuilder implements ICloseable {

	private final IdBuilder idBuilder = IdFactory.newInstance();
	private final AtomicLong id = new AtomicLong(-1L);
	private final AtomicReference<Process> cache = new AtomicReference<>();
	private final IShellFactory factory;
	private final ProcessBuilder builder;
	private Charset charset = StandardCharsets.UTF_8;
	private Duration timeout = Duration.ZERO;

	public ShellBuilder(IShellFactory factory, String[] command) throws ValidateException {
		ValidateUtils.notNull("ShellBuilder", factory);
		this.factory = factory;
		builder = new ProcessBuilder();
		command(command);
	}

	public void command(String[] command) {
		if (!ArrayUtils.isEmpty(command)) {
			builder.command(ArrayUtils.merge(PizContext.LOCAL_OS.getEnvironment(), command));
		}
	}

	public String getId() {
		return StringUtils.of(id);
	}

	public ShellBuilder charset(Charset charset) {
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
	public ShellBuilder waitFor(Duration timeout) {
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
		if (cache.get() != null && cache.get().isAlive()) {
			if (force) {
				cache.get().destroyForcibly();
			} else {
				cache.get().destroy();
			}
		}
		this.id.set(idBuilder.generate());
		cache.set(process);
	}

	public FutureResult<List<String>> execute(IMessageOutput<String> outputS, IMessageOutput<String> outputE)
			throws BaseException {
		builder.redirectErrorStream(false);
		// 运行一个新的子进程
		return factory.newProcess(builder, timeout, process -> {
			// 强制关闭上一个执行子进程，将当前进程设置为临时进程
			turn(ShellBuilder.this.id.get(), process, true);
			FutureResult<List<String>> result = new FutureResult<>(
					factory.apply(process.getInputStream(), charset, outputS),
					factory.apply(process.getErrorStream(), charset, outputE));
			CompletableFuture.allOf(result.getInputFuture(), result.getErrorFuture()).whenCompleteAsync((v, e) ->
					turn(ShellBuilder.this.id.get(), null, false), factory.getThreadPool());
			return result;
		});
	}

	public CompletableFuture<List<String>> execute(IMessageOutput<String> output) throws BaseException {
		builder.redirectErrorStream(true);
		// 运行一个新的子进程
		return factory.newProcess(builder, timeout, process -> {
			// 强制关闭上一个执行子进程，将当前进程设置为临时进程
			turn(ShellBuilder.this.id.get(), process, true);
			return factory.apply(process.getInputStream(), charset, output).thenApply(v -> {
				// 执行完成后关闭当前子进程
				turn(ShellBuilder.this.id.get(), null, false);
				return v;
			});
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

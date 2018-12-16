package org.pizazz.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.IOUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.MessageOutputHelper;
import org.pizazz.message.ref.TypeEnum;
import org.pizazz.tool.ref.IShellFactory;

/**
 * SHELL工厂组件
 * 
 * @author xlgp2171
 * @version 1.1.181216
 */
public final class PShellFactory implements IShellFactory, ICloseable {

	private final ExecutorService threadPool;

	public PShellFactory() {
		int _maximumPoolSize = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_SHELL_POOL_MAX", 16);
		long _keepAliveTime = ConfigureHelper.getLong(TypeEnum.BASIC, "DEF_SHELL_THREAD_KEEP", 60L);
		threadPool = new ThreadPoolExecutor(0, _maximumPoolSize, _keepAliveTime, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new PThreadFactory(Constant.NAMING_SHORT + "-shell", true));
	}

	public static PShellBuilder newInstance(String... command) throws BaseException {
		return new PShellBuilder(Singleton.INSTANCE.get(), command);
	}

	@Override
	public Process newProcess(ProcessBuilder builder, int timeout) throws BaseException {
		Process _process;
		try {
			_process = builder.start();
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.START", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		try {
			if (timeout == 0) {
				_process.waitFor();
			} else if (timeout > 0) {
				_process.waitFor(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.WAIT", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		return _process;
	}

	@Override
	public CompletableFuture<List<String>> apply(InputStream in, Charset charset, IMessageOutput<String> output) {
		return CompletableFuture.supplyAsync(
				new StreamSupplier(in, charset, output == null ? MessageOutputHelper.EMPTY_STRING_ENABLE : output),
				threadPool);
	}

	@Override
	public ExecutorService getExecutorService() {
		return threadPool;
	}

	@Override
	public void destroy(int timeout) throws BaseException {
		threadPool.shutdownNow();
	}

	private class StreamSupplier implements Supplier<List<String>> {
		private final InputStream in;
		private final Charset charset;
		private final IMessageOutput<String> call;

		public StreamSupplier(InputStream in, Charset charset, IMessageOutput<String> call) {
			this.in = in;
			this.charset = charset;
			this.call = call;
		}

		@Override
		public List<String> get() {
			final List<String> _tmp = new LinkedList<String>();
			IOUtils.readLine(in, charset, new IMessageOutput<String>() {
				@Override
				public void write(String message) {
					if (call.isEnable()) {
						_tmp.add(message);
					}
					call.write(message);
				}

				@Override
				public void throwException(Exception e) {
					call.throwException(e);
				};
			});
			return _tmp;
		}
	}

	public static enum Singleton {
		INSTANCE;

		private PShellFactory factory;

		private Singleton() {
			factory = new PShellFactory();
		}

		public PShellFactory get() {
			return factory;
		}
	}
}

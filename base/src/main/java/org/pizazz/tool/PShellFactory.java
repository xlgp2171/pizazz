package org.pizazz.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.common.IOUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ConfigureHelper;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;
import org.pizazz.tool.ref.IShellFactory;

/**
 * SHELL工厂组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public final class PShellFactory implements IShellFactory, ICloseable {

	private final ThreadPoolExecutor threadPool;

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
		if (timeout == 0) {
			try {
				_process.waitFor();
			} catch (InterruptedException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.WAIT", e.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
			}
		} else if (timeout > 0) {
			try {
				_process.waitFor(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.WAIT", e.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
			}
		}
		return _process;
	}

	@Override
	public Future<List<String>> submit(InputStream in, Charset charset, ICloseable shutdown,
			IMessageOutput<String> output) {
		return threadPool
				.submit(new StreamCallable(in, charset, shutdown, output == null ? new IMessageOutput<String>() {
					@Override
					public boolean isEnable() {
						return true;
					}

					@Override
					public void write(String message) {
					}
				} : output));
	}

	@Override
	public void destroy(int timeout) throws BaseException {
		threadPool.shutdownNow();
	}

	private class StreamCallable implements Callable<List<String>> {
		private final InputStream in;
		private final ICloseable shutdown;
		private final Charset charset;
		private final IMessageOutput<String> call;

		public StreamCallable(InputStream in, Charset charset, ICloseable shutdown, IMessageOutput<String> call) {
			this.in = in;
			this.charset = charset;
			this.shutdown = shutdown;
			this.call = call;
		}

		@Override
		public List<String> call() throws Exception {
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
			shutdown.destroy(0);
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

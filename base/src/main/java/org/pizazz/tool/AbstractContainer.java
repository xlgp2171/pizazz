package org.pizazz.tool;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.pizazz.Constant;
import org.pizazz.IMessageOutput;
import org.pizazz.IPlugin;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.SystemUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.TypeEnum;

/**
 * 容器组件超类
 * 
 * @param <T> 输出类型
 *
 * @author xlgp2171
 * @version 1.1.190202
 */
public abstract class AbstractContainer<T> implements IPlugin {
	public static final String KEY_CONTAINER_TIMEOUT = "$TIMEOUT";

	protected final TupleObject properties_;
	protected final IPlugin plugin_;
	protected final IMessageOutput<T> output_;

	private final Callable<Integer> callable = new Callable<Integer>() {
		@Override
		public Integer call() throws Exception {
			try {
				plugin_.destroy(Duration.ZERO);
			} catch (Exception e) {
				output_.throwException(e);
				return -1;
			}
			return 0;
		}
	};

	public AbstractContainer(IPlugin plugin, IMessageOutput<T> output) throws BaseException {
		AssertUtils.assertNotNull("AbstractContainer", plugin, output);
		properties_ = TupleObjectHelper.newObject(4);
		this.plugin_ = plugin;
		this.output_ = output;
	}

	public void waitForShutdown() {
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			output_.throwException(e);
		}
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
		properties_.append(KEY_CONTAINER_TIMEOUT, ConfigureHelper.getConfig(TypeEnum.BASIC,
				Constant.NAMING_SHORT + ".kc.timeout", "DEF_CONTAINER_TIMEOUT", "30000"));
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		int _status = 0;

		if (timeout == null || timeout.isNegative()) {
			int _maxTimeout = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_CONTAINER_TIMEOUT_MAX", 60000);
			int _exitTime = TupleObjectHelper.getInt(properties_, KEY_CONTAINER_TIMEOUT, 20000);
			timeout = Duration.ofMillis((_exitTime > 0 && _exitTime <= _maxTimeout) ? _exitTime : _maxTimeout);
		}
		if (timeout.isZero()) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.DESTROY");
			SystemUtils.println(System.out, new StringBuffer(_msg));
			try {
				_status = callable.call();
			} catch (Exception e) {
				output_.throwException(e);
			}
		} else {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.DESTROY.TIMEOUT", timeout.toMillis());
			SystemUtils.println(System.out, new StringBuffer(_msg));
			try {
				_status = Executors.newSingleThreadExecutor().submit(callable).get(timeout.toMillis(),
						TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				_msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONTAINER.TIMEOUT", timeout);
				SystemUtils.println(System.err, new StringBuffer(_msg));
				_status = -2;
			} catch (Exception e) {
				_status = -3;
				e.printStackTrace();
				Runtime.getRuntime().halt(_status);
				return;
			} finally {
				SystemUtils.destroy(output_, Duration.ZERO);
			}
		}
		Runtime.getRuntime().exit(_status);
	}
}

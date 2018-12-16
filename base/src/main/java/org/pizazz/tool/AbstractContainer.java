package org.pizazz.tool;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.pizazz.Constant;
import org.pizazz.IMessageOutput;
import org.pizazz.IPlugin;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.SystemUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.ref.TypeEnum;

/**
 * 容器组件超类
 * 
 * @param <T> 输出类型
 *
 * @author xlgp2171
 * @version 1.0.181210
 */
public abstract class AbstractContainer<T> implements IPlugin {
	static final String KEY_CONTAINER_PORT = "$PORT";
	static final String KEY_CONTAINER_KEY = "$KEY";
	static final String KEY_CONTAINER_TIMEOUT = "$TIMEOUT";

	protected final TupleObject properties;
	protected final IPlugin plugin;
	protected final IMessageOutput<T> output;

	private final Callable<Integer> callable = new Callable<Integer>() {
		@Override
		public Integer call() throws Exception {
			try {
				plugin.destroy(-1);
			} catch (Exception e) {
				output.throwException(e);
				return -1;
			}
			return 0;
		}
	};

	public AbstractContainer(IPlugin plugin, IMessageOutput<T> output) throws BaseException {
		AssertUtils.assertNotNull("AbstractContainer", plugin, output);
		properties = TupleObjectHelper.newObject(4);
		this.plugin = plugin;
		this.output = output;
	}

	public abstract void waitForShutdown();

	@Override
	public void initialize(TupleObject config) throws BaseException {
		properties
				.append(KEY_CONTAINER_PORT,
						ConfigureHelper.getConfig(TypeEnum.BASIC, Constant.NAMING_SHORT + ".kc.port",
								"DEF_CONTAINER_PORT", "10420"))
				.append(KEY_CONTAINER_KEY,
						ConfigureHelper.getConfig(TypeEnum.BASIC, Constant.NAMING_SHORT + ".kc.key",
								"DEF_CONTAINER_KEY", SystemUtils.newUUIDSimple()))
				.append(KEY_CONTAINER_TIMEOUT, ConfigureHelper.getConfig(TypeEnum.BASIC,
						Constant.NAMING_SHORT + ".kc.timeout", "DEF_CONTAINER_TIMEOUT", "30000"));
	}

	@Override
	public void destroy(int timeout) {
		int _status = 0;

		if (timeout <= 0) {
			try {
				_status = callable.call();
			} catch (Exception e) {
				output.throwException(e);
			}
		} else {
			try {
				_status = Executors.newSingleThreadExecutor().submit(callable).get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONTAINER.TIMEOUT", timeout);
				SystemUtils.println(System.err, new StringBuilder(_msg));
				_status = -2;
			} catch (Exception e) {
				_status = -3;
				e.printStackTrace();
				Runtime.getRuntime().halt(_status);
				return;
			} finally {
				IOUtils.close(output, 0);
			}
		}
		Runtime.getRuntime().exit(_status);
	}
}

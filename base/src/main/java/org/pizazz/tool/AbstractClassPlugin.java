package org.pizazz.tool;

import java.time.Duration;

import org.pizazz.IPlugin;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.StringUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.context.PluginContext;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AbstractException;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 通用加载器组件
 * 
 * @author xlgp2171
 * @version 1.1.190220
 */
public abstract class AbstractClassPlugin implements IPlugin {
	private final TupleObject configure = TupleObjectHelper.newObject();

	protected abstract void log(String msg, BaseException e);

	protected final TupleObject setConfig(TupleObject config) {
		if (config != null) {
			synchronized (configure) {
				configure.putAll(config);
			}
		}
		return configure;
	}

	protected final TupleObject updateConfig(TupleObject config) {
		synchronized (configure) {
			configure.clear();
		}
		return setConfig(config);
	}

	protected final TupleObject getConfig() {
		return configure;
	}

	protected final TupleObject copyConfig() {
		return configure.clone();
	}

	/**
	 * 
	 * @param plugin 插件类
	 * @param clazz 需要转换的类型,需实现org.pizazz.IPlugin接口
	 * @return
	 * @throws UtilityException 类型转换失败
	 * @throws AssertException 若参数任意为null时
	 */
	public <T extends IPlugin> T cast(IPlugin plugin, Class<T> clazz) throws AssertException, UtilityException {
		AssertUtils.assertNotNull("cast", plugin, clazz);
		return ClassUtils.cast(plugin, clazz);
	}

	/**
	 * 加载插件
	 * 
	 * @param key 在配置中的键值或者classpath路径
	 * @param defPlugin 默认的Plugin实现,可为null
	 * @param loader 采用的类加载器,null为默认加载器
	 * @param initialize 是否加载后调用初始化方法
	 * @return 加载后的实现类,可用cast方法转换类型
	 * @throws AbstractException
	 */
	public IPlugin loadPlugin(String key, IPlugin defPlugin, ClassLoader loader, boolean initialize)
			throws AbstractException {
		String _classpath = TupleObjectHelper.getString(configure, key, "");
		try {
			return load(_classpath, key, defPlugin, loader, initialize, null);
		} catch (BaseException e) {
			if (e.getMessage().startsWith(BasicCodeEnum.MSG_0014.getCode())) {
				throw e;
			}
			log(e.getMessage(), e);
			return load("", key, defPlugin, loader, initialize, e);
		}
	}

	protected IPlugin load(String classpath, String defClass, IPlugin defPlugin, ClassLoader loader, boolean initialize,
			BaseException e) throws AbstractException {
		if (StringUtils.isTrimEmpty(classpath)) {
			if (defPlugin != null) {
				log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.LOAD", defPlugin.getId()), null);
				return initialize ? initPlugin(defPlugin) : defPlugin;
			}
			if (StringUtils.isTrimEmpty(defClass)) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PLUGIN.LOAD", "");
				throw new ToolException(BasicCodeEnum.MSG_0014, _msg, e);
			}
			classpath = defClass;
		}
		IPlugin _tmp = ClassUtils.newClass(classpath, loader, IPlugin.class);
		return switchPlugin(_tmp, initialize);
	}

	protected IPlugin switchPlugin(IPlugin instance, boolean initialize) throws AbstractException {
		PluginContext.getInstance().register(getClass(), instance);
		log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.REGISTER", instance.getId()), null);
		return initialize ? initPlugin(instance) : instance;
	}

	protected IPlugin initPlugin(IPlugin instance) throws AbstractException {
		instance.initialize(copyConfig());
		log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.INIT", instance.getId()), null);
		return instance;
	}

	public void unloadPlugin(IPlugin plugin, Duration timeout) {
		if (plugin != null) {
			SystemUtils.destroy(plugin, timeout);
			PluginContext.getInstance().unregister(getClass(), plugin);
			log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.UNLOAD", plugin.getId()), null);
		}
	}
}

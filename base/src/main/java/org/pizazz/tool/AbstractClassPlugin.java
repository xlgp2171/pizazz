package org.pizazz.tool;

import org.pizazz.IPlugin;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.TupleObjectUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.context.PluginContext;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;

/**
 * 通用加载器组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public abstract class AbstractClassPlugin implements IPlugin {
	private final TupleObject configure = TupleObjectUtils.newObject();

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
		return configure.clone();
	}

	/**
	 * 
	 * @param plugin 插件类
	 * @param clazz 需要转换的类型,需实现org.pizazz.IPlugin接口
	 * @return
	 * @throws BaseException 若参数任意为null时
	 */
	public <T extends IPlugin> T cast(IPlugin plugin, Class<T> clazz) throws BaseException {
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
	 * @throws BaseException
	 */
	public IPlugin loadPlugin(String key, IPlugin defPlugin, ClassLoader loader, boolean initialize)
			throws BaseException {
		String _classpath = TupleObjectUtils.getString(configure, key, "");
		try {
			return load(_classpath, key, defPlugin, loader, initialize, null);
		} catch (BaseException e) {
			if (e.getMessage().startsWith(BasicCodeEnum.MSG_0014.getValue())) {
				throw e;
			}
			log(e.getMessage(), e);
			return load("", key, defPlugin, loader, initialize, e);
		}
	}

	protected IPlugin load(String classpath, String defClass, IPlugin defPlugin, ClassLoader loader, boolean initialize,
			BaseException e) throws BaseException {
		if (StringUtils.isTrimEmpty(classpath)) {
			if (defPlugin != null) {
				log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.LOAD", defPlugin.getId()), null);
				return initialize ? initPlugin(defPlugin) : defPlugin;
			}
			if (StringUtils.isTrimEmpty(defClass)) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PLUGIN.LOAD", "");
				throw new BaseException(BasicCodeEnum.MSG_0014, _msg, e);
			}
			classpath = defClass;
		}
		IPlugin _tmp = ClassUtils.newClass(classpath, loader, IPlugin.class);
		return switchPlugin(_tmp, initialize);
	}

	protected IPlugin switchPlugin(IPlugin instance, boolean initialize) throws BaseException {
		PluginContext.getInstance().register(getClass(), instance);
		log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.REGISTER", instance.getId()), null);
		return initialize ? initPlugin(instance) : instance;
	}

	protected IPlugin initPlugin(IPlugin instance) throws BaseException {
		instance.initialize(getConfig());
		log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.INIT", instance.getId()), null);
		return instance;
	}

	public void unloadPlugin(IPlugin plugin, int timeout) {
		if (plugin != null) {
			IOUtils.close(plugin, timeout);
			PluginContext.getInstance().unregister(getClass(), plugin);
			log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.UNLOAD", plugin.getId()), null);
		}
	}
}
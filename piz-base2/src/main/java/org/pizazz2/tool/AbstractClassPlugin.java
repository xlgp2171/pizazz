package org.pizazz2.tool;

import org.pizazz2.IObject;
import org.pizazz2.IPlugin;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.context.PluginContext;
import org.pizazz2.exception.*;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import java.time.Duration;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 通用加载器组件
 *
 * @param <C> 配置文件类型
 * @author xlgp2171
 * @version 2.1.211201
 */
public abstract class AbstractClassPlugin<C extends IObject> {
    private final C configure;

    public AbstractClassPlugin(C configure) throws ValidateException {
        ValidateUtils.notNull("AbstractClassPlugin", configure);
        this.configure = configure;
    }

    /**
     * 日志记录
     *
     * @param message 日志消息
     * @param exception 异常输出
     */
    protected abstract void log(String message, BaseException exception);

    protected final C setConfig(C config) {
        if (config != null) {
            synchronized (configure) {
                configure.set(null, config);
            }
        }
        return configure;
    }

    protected final C updateConfig(C config) {
        synchronized (configure) {
            configure.reset();
        }
        return setConfig(config);
    }

    public final C getConfig() {
        return configure;
    }

    /**
     * 插件转换
     *
     * @param plugin 插件类
     * @param clazz 需要转换的类型,需实现{@link IPlugin}接口
     * @param <T> 实现{@link IPlugin}接口的类
     * @return 转换后的类型
     *
     * @throws UtilityException 类型转换失败
     * @throws ValidateException 若参数任意为null时
     * @throws IllegalException 类型转换异常
     */
    public <T extends IPlugin<?>> T cast(IPlugin<?> plugin, Class<T> clazz)
            throws ValidateException, IllegalException, UtilityException {
        ValidateUtils.notNull("cast", plugin, clazz);
        return ClassUtils.cast(plugin, clazz);
    }

    /**
     * 加载插件
     * <li/> 首先尝试采用ServiceLoader加载
     *
     * @param key 在配置中的键值或者classpath路径
     * @param clazz 接口的Class
     * @param loader 采用的类加载器,null为默认加载器
     * @param initialize 是否加载后调用初始化方法
     * @param <T> 实现{@link IPlugin}接口的类
     * @return 加载后的实现类, 可用cast方法转换类型
     *
     * @throws BaseException 插件类型转换异常，插件不存在或插件初始化异常
     * @throws ValidateException 参数验证异常
     * @throws IllegalException 类型转换异常
     */
    public <T extends IPlugin<C>> T loadPlugin(String key, Class<T> clazz, ClassLoader loader, boolean initialize)
            throws BaseException, ValidateException, IllegalException {
        ValidateUtils.notNull("loadPlugin", 0, clazz);
        Iterator<T> iterator = ServiceLoader.load(clazz,
                loader == null ? ClassUtils.getClassLoader() : loader).iterator();
        T tmp = null;
        try {
            if (iterator.hasNext()) {
                tmp = iterator.next();
            }
        } catch (Throwable e) {
            ToolException exception = new ToolException(BasicCodeEnum.MSG_0014,
                    LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PLUGIN.LOAD", clazz.getName()), e);
			log(exception.getMessage(), exception);
        }
        return cast(loadPlugin(key, tmp, loader, initialize), clazz);
    }

    /**
     * 加载插件
     *
     * @param key 在配置中的键值或者classpath路径
     * @param defPlugin 默认的Plugin实现,可为null
     * @param loader 采用的类加载器,null为默认加载器
     * @param initialize 是否加载后调用初始化方法
     * @return 加载后的实现类, 可用cast方法转换类型
     *
     * @throws BaseException 插件类型转换异常，插件不存在或插件初始化异常
     * @throws ValidateException 参数验证异常
     */
    public IPlugin<C> loadPlugin(String key, IPlugin<C> defPlugin, ClassLoader loader, boolean initialize)
            throws BaseException, ValidateException {
        String classpath = StringUtils.of(configure.get(key, StringUtils.EMPTY));
        try {
            return load(classpath, key, defPlugin, loader, initialize, null);
        } catch (BaseException e) {
            log(e.getMessage(), e);
            return load(StringUtils.EMPTY, key, defPlugin, loader, initialize, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected IPlugin<C> load(String classpath, String defClass, IPlugin<C> defPlugin, ClassLoader loader, boolean initialize,
                              BaseException e) throws BaseException, IllegalException {
        if (StringUtils.isTrimEmpty(classpath)) {
            if (defPlugin != null) {
                log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.LOAD", defPlugin.getId()), null);
                return initialize ? initialPlugin(defPlugin) : defPlugin;
            } else if (StringUtils.isTrimEmpty(defClass)) {
                if (e != null) {
                    throw e;
                } else {
                    String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PLUGIN.LOAD", StringUtils.EMPTY);
                    throw new IllegalException(BasicCodeEnum.MSG_0014, msg);
                }
            }
            classpath = defClass;
        }
        IPlugin<C> tmp = (IPlugin<C>) ClassUtils.newClass(classpath, loader, IPlugin.class);
        log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.LOAD", tmp.getId()), null);
        return switchPlugin(tmp, initialize);
    }

    protected IPlugin<C> switchPlugin(IPlugin<C> instance, boolean initialize) throws BaseException {
        PluginContext.getInstance().register(getClass(), instance);
        log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.REGISTER", instance.getId()), null);
        return initialize ? initialPlugin(instance) : instance;
    }

    @SuppressWarnings("unchecked")
    protected IPlugin<C> initialPlugin(IPlugin<C> instance) throws BaseException {
        instance.initialize((C) configure.copy());
        log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.INIT", instance.getId()), null);
        return instance;
    }

    public void unloadPlugin(IPlugin<?> plugin, Duration timeout) {
        if (plugin != null) {
            SystemUtils.destroy(plugin, timeout);
            PluginContext.getInstance().unregister(getClass(), plugin);
            log(LocaleHelper.toLocaleText(TypeEnum.BASIC, "PLUGIN.UNLOAD", plugin.getId()), null);
        }
    }
}

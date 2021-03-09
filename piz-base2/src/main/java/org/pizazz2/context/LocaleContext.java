package org.pizazz2.context;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz2.Constant;
import org.pizazz2.ICloseable;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.message.ref.IType;

/**
 * 国际化消息环境组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public final class LocaleContext implements ICloseable {
    private final ConcurrentMap<IType, Map<Locale, Properties>> tree;

    public static LocaleContext getInstance() {
        return Singleton.INSTANCE.get();
    }

    public LocaleContext() {
        tree = new ConcurrentHashMap<>();
        register(TypeEnum.BASIC, SystemUtils.LOCAL_LOCALE);
    }

    public LocaleContext register(IType type, Locale locale) {
        if (!tree.containsKey(type)) {
            synchronized (tree) {
                if (!tree.containsKey(type)) {
                    ConcurrentMap<Locale, Properties> tmp = new ConcurrentHashMap<>();
                    load(tmp, type, locale);
                    tree.put(type, tmp);
                } else {
                    Map<Locale, Properties> tmp = tree.get(type);

                    if (!tmp.containsKey(locale)) {
                        load(tmp, type, locale);
                    }
                }
            }
        } else {
            Map<Locale, Properties> tmp = tree.get(type);

            if (tmp != null && !tmp.containsKey(locale)) {
                synchronized (tmp) {
                    if (!tmp.containsKey(locale)) {
                        load(tmp, type, locale);
                    }
                }
            }
        }
        return this;
    }

    public LocaleContext unregister(IType type) {
        if (type != null) {
            if (tree.containsKey(type)) {
                synchronized (tree) {
                    if (tree.containsKey(type)) {
                        tree.remove(type).clear();
                    }
                }
            }
        }
        return this;
    }

    public Map<IType, Map<Locale, Properties>> getLocaleProperties() {
        return tree;
    }

    public Properties getProperties(IType type, Locale locale) {
        Map<Locale, Properties> tmp = tree.get(type);

        if (tmp != null) {
            return tmp.get(locale);
        }
        return new Properties();
    }

    private void load(Map<Locale, Properties> target, IType type, Locale locale) {
        String postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".locale.postfix", "_Locale");
        ResourceBundle resource = ResourceBundle.getBundle(type.value() + postfix, locale);
        Properties tmp = new Properties();
        resource.keySet().forEach(item -> tmp.setProperty(item, resource.getString(item)));
        target.put(locale, tmp);
    }

    @Override
    public void destroy(Duration timeout) {
        tree.values().forEach(Map::clear);
        tree.clear();
    }

    private static enum Singleton {
        /**
         * 单例
         */
        INSTANCE;

        private final LocaleContext context;

        private Singleton() {
            context = new LocaleContext();
        }

        public LocaleContext get() {
            return context;
        }
    }
}

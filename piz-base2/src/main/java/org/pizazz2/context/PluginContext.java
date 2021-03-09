package org.pizazz2.context;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.pizazz2.ICloseable;
import org.pizazz2.IPlugin;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.ObjectUtils;

/**
 * 插件环境组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public final class PluginContext implements ICloseable {
	/**
	 * 插件缓存
	 */
    private final ConcurrentMap<Class<?>, Set<WeakReference<IPlugin>>> tree;
    private final Object lock = new Object();
    private final Comparator<WeakReference<IPlugin>> cr = (o1, o2) -> {
        if (!ObjectUtils.isNull(o1) && !ObjectUtils.isNull(o2)) {
            if (o1.get().hashCode() > o2.get().hashCode()) {
                return 1;
            } else if (o1.get().hashCode() < o2.get().hashCode()) {
                return -1;
            }
        } else if (ObjectUtils.isNull(o1)) {
            return -1;
        } else if (ObjectUtils.isNull(o2)) {
            return 1;
        }
        return 0;
    };

    private PluginContext() {
        tree = new ConcurrentHashMap<>();
    }

    public static PluginContext getInstance() {
        return Singleton.INSTANCE.get();
    }

    public void create(Class<?>... types) {
        synchronized (lock) {
            for (Class<?> item : types) {
                if (!tree.containsKey(item)) {
                    tree.put(item, new ConcurrentSkipListSet<>(cr));
                }
            }
        }
    }

    public void register(Class<?> type, IPlugin plugin) {
        if (type != null && plugin != null) {
            getByType(type).add(new WeakReference<>(plugin));
        }
    }

    public void unregister(Class<?> type, IPlugin plugin) {
        if (type != null && plugin != null) {
            getByType(type).remove(new WeakReference<>(plugin));
        }
    }

    public Set<WeakReference<IPlugin>> getByType(Class<?> type) {
        if (type == null) {
            return CollectionUtils.emptySet();
        } else if (!tree.containsKey(type)) {
            create(type);
        }
        return tree.get(type);
    }

    public Map<Class<?>, Set<WeakReference<IPlugin>>> getPluginTree() {
        return tree;
    }

    @Override
    public void destroy(Duration timeout) {
        synchronized (lock) {
            tree.values().forEach(Set::clear);
            tree.clear();
        }
    }

    private static enum Singleton {
		/**
		 * 单例
		 */
		INSTANCE;

        private final PluginContext context;

        private Singleton() {
            context = new PluginContext();
        }

        public PluginContext get() {
            return context;
        }
    }
}

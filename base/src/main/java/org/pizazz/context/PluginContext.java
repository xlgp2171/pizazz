package org.pizazz.context;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.pizazz.ICloseable;
import org.pizazz.IPlugin;

/**
 * 插件环境组件
 * 
 * @author xlgp2171
 * @version 1.0.181216
 */
public final class PluginContext implements ICloseable {
	private final ConcurrentMap<Class<?>, Set<WeakReference<IPlugin>>> tree;
	private final Object lock = new Object();

	private PluginContext() {
		tree = new ConcurrentHashMap<Class<?>, Set<WeakReference<IPlugin>>>();
	}

	public static final PluginContext getInstance() {
		return Singleton.INSTANCE.get();
	}

	public void create(Class<?>... types) {
		synchronized (lock) {
			for (Class<?> _item : types) {
				if (!tree.containsKey(_item)) {
					tree.put(_item, new ConcurrentSkipListSet<WeakReference<IPlugin>>());
				}
			}
		}
	}

	public void register(Class<?> type, IPlugin plugin) {
		if (type != null && plugin != null) {
			getByType(type).add(new WeakReference<IPlugin>(plugin));
		}
	}

	public void unregister(Class<?> type, IPlugin plugin) {
		if (type != null && plugin != null) {
			getByType(type).remove(new WeakReference<IPlugin>(plugin));
		}
	}

	public Set<WeakReference<IPlugin>> getByType(Class<?> type) {
		if (type == null) {
			return null;
		}
		if (!tree.containsKey(type)) {
			create(type);
		}
		return tree.get(type);
	}

	public Map<Class<?>, Set<WeakReference<IPlugin>>> getPluginTree() {
		return tree;
	}

	public void destroy(int timeout) {
		synchronized (lock) {
			tree.values().stream().forEach(_item -> _item.clear());
			tree.clear();
		}
	}

	private static enum Singleton {
		INSTANCE;

		private PluginContext context;

		private Singleton() {
			context = new PluginContext();
		}

		public PluginContext get() {
			return context;
		}
	}
}

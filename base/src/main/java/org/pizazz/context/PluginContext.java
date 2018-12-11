package org.pizazz.context;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz.IPlugin;

/**
 * 插件环境组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public final class PluginContext {
	private final ConcurrentMap<Class<?>, Set<WeakReference<IPlugin>>> tree;
	private final Object lock = new Object();

	private PluginContext() {
		tree = new ConcurrentHashMap<Class<?>, Set<WeakReference<IPlugin>>>();
	}

	public static final PluginContext getInstance() {
		return Singleton.INSTANCE.get();
	}

	public void create(Class<?>... type) {
		synchronized (lock) {
			for (Class<?> _item : type) {
				if (!tree.containsKey(type)) {
					Set<WeakReference<IPlugin>> _tmp = Collections
					        .synchronizedSet(
					                new HashSet<WeakReference<IPlugin>>());
					tree.put(_item, _tmp);
				}
			}
		}
	}

	public void register(Class<?> type, IPlugin plugin) {
		getByType(type).add(new WeakReference<IPlugin>(plugin));
	}

	public void unregister(Class<?> type, IPlugin plugin) {
		getByType(type).remove(new WeakReference<IPlugin>(plugin));
	}

	public Set<WeakReference<IPlugin>> getByType(Class<?> type) {
		if (!tree.containsKey(type)) {
			create(type);
		}
		return tree.get(type);
	}

	public void clear() {
		synchronized (lock) {
			for (Set<WeakReference<IPlugin>> _item : tree.values()) {
				_item.clear();
			}
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

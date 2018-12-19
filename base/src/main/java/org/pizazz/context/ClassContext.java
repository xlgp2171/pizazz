package org.pizazz.context;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz.ICloseable;
import org.pizazz.exception.BaseException;
import org.pizazz.tool.PClassLoader;

/**
 * 类环境组件
 * 
 * @author xlgp2171
 * @version 1.0.181219
 */
public final class ClassContext implements ICloseable {
	private final ConcurrentMap<String, WeakReference<PClassLoader>> loaders;
	private final ConcurrentMap<URL, Set<String>> tree;
	private final Object lock = new Object();

	private ClassContext() {
		loaders = new ConcurrentHashMap<String, WeakReference<PClassLoader>>();
		tree = new ConcurrentHashMap<URL, Set<String>>();
	}

	public static final ClassContext getInstance() {
		return Singleton.INSTANCE.get();
	}

	private void createID(PClassLoader loader) {
		URL[] _urls = loader.getURLs();

		for (URL _item : _urls) {
			if (!tree.containsKey(_item)) {
				tree.put(_item, new HashSet<String>());
			}
			tree.get(_item).add(loader.getId());
		}
	}

	private void removeID(PClassLoader loader) {
		URL[] _urls = loader.getURLs();

		for (URL _item : _urls) {
			if (tree.containsKey(_item)) {
				tree.get(_item).remove(loader.getId());
			}
		}
	}

	private void removeID(String id) {
		tree.values().stream().forEach(_item -> {
			if (_item.contains(id)) {
				_item.remove(id);
			}
		});
	}

	public PClassLoader register(PClassLoader loader) {
		if (loader == null) {
			return null;
		}
		synchronized (lock) {
			String _id = loader.getId();

			if (loaders.containsKey(_id)) {
				PClassLoader _loader = loaders.get(_id).get();

				if (_loader != null) {
					return _loader;
				}
			}
			loaders.put(_id, new WeakReference<PClassLoader>(loader));
			createID(loader);
		}
		return loader;
	}

	public PClassLoader unregister(String id) {
		PClassLoader _loader = null;

		synchronized (lock) {
			if (loaders.containsKey(id)) {
				_loader = loaders.remove(id).get();

				if (_loader != null) {
					removeID(_loader);
					_loader.close();
				} else {
					removeID(id);
				}
			}
		}
		return _loader;
	}

	public PClassLoader getLoader(String id) {
		if (!loaders.containsKey(id)) {
			return null;
		}
		PClassLoader _loader = loaders.get(id).get();

		if (_loader == null) {
			clearID(id);
		}
		return _loader;
	}

	public void clearID(String id) {
		synchronized (lock) {
			loaders.remove(id);
			removeID(id);
		}
	}

	public Map<URL, Set<String>> getClassTree() {
		return tree;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		synchronized (lock) {
			String[] _tmp = new String[loaders.size()];
			_tmp = loaders.keySet().toArray(_tmp);

			for (String _item : _tmp) {
				unregister(_item);
			}
			tree.clear();
		}
	}

	private static enum Singleton {
		INSTANCE;

		private ClassContext context;

		private Singleton() {
			context = new ClassContext();
		}

		public ClassContext get() {
			return context;
		}
	}
}

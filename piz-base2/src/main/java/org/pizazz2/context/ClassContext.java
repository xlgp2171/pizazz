package org.pizazz2.context;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz2.ICloseable;
import org.pizazz2.tool.PizClassLoader;

/**
 * 类环境组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public final class ClassContext implements ICloseable {
    private final ConcurrentMap<String, WeakReference<PizClassLoader>> loaders;
    private final ConcurrentMap<URL, Set<String>> tree;
    private final Object lock = new Object();

    private ClassContext() {
        loaders = new ConcurrentHashMap<>();
        tree = new ConcurrentHashMap<>();
    }

    public static ClassContext getInstance() {
        return Singleton.INSTANCE.get();
    }

    private void addId(PizClassLoader loader) {
        URL[] urls = loader.getURLs();

        for (URL item : urls) {
            if (!tree.containsKey(item)) {
                tree.put(item, new HashSet<>());
            }
            tree.get(item).add(loader.getId());
        }
    }

    private void removeId(PizClassLoader loader) {
        URL[] urls = loader.getURLs();

        for (URL item : urls) {
            if (tree.containsKey(item)) {
                tree.get(item).remove(loader.getId());
            }
        }
    }

    private void removeId(String id) {
        tree.values().forEach(item -> item.remove(id));
    }

    public PizClassLoader register(PizClassLoader loader) {
        if (loader == null) {
            return null;
        }
        synchronized (lock) {
            String id = loader.getId();

            if (loaders.containsKey(id)) {
                PizClassLoader tmp = loaders.get(id).get();

                if (tmp != null) {
                    return tmp;
                }
            }
            loaders.put(id, new WeakReference<>(loader));
            addId(loader);
        }
        return loader;
    }

    public void unregister(String id) {
        PizClassLoader loader = null;

        synchronized (lock) {
            if (loaders.containsKey(id)) {
                loader = loaders.remove(id).get();

                if (loader != null) {
                    removeId(loader);
                    loader.close();
                } else {
                    removeId(id);
                }
            }
        }
	}

    public PizClassLoader getLoader(String id) {
        if (!loaders.containsKey(id)) {
            return null;
        }
        PizClassLoader loader = loaders.get(id).get();

        if (loader == null) {
            clearId(id);
        }
        return loader;
    }

    public void clearId(String id) {
        synchronized (lock) {
            loaders.remove(id);
            removeId(id);
        }
    }

    public Map<URL, Set<String>> getClassTree() {
        return tree;
    }

    @Override
    public void destroy(Duration timeout) {
        synchronized (lock) {
            String[] tmp = new String[loaders.size()];
            tmp = loaders.keySet().toArray(tmp);

            for (String item : tmp) {
                unregister(item);
            }
            tree.clear();
        }
    }

    private static enum Singleton {
        /**
         * 单例
         */
        INSTANCE;

        private final ClassContext context;

        private Singleton() {
            context = new ClassContext();
        }

        public ClassContext get() {
            return context;
        }
    }
}

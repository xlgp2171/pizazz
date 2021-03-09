package org.pizazz2.context;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz2.Constant;
import org.pizazz2.ICloseable;
import org.pizazz2.common.ResourceUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.BaseException;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.message.ref.IType;

/**
 * 内部配置环境组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public final class ConfigureContext implements ICloseable {
	private final ConcurrentMap<IType, Properties> tree = new ConcurrentHashMap<>();

	public static ConfigureContext getInstance() {
		return Singleton.INSTANCE.get();
	}

	public ConfigureContext() {
		register(TypeEnum.BASIC);
	}

	public void register(IType type) {
		if (!tree.containsKey(type)) {
			synchronized (tree) {
				if (!tree.containsKey(type)) {
					load(type);
				}
			}
		}
	}

	public void unregister(IType type) {
		if (type != null) {
			if (tree.containsKey(type)) {
				synchronized (tree) {
					if (tree.containsKey(type)) {
						tree.remove(type).clear();
					}
				}
			}
		}
	}

	public Map<IType, Properties> getConfigureTree() {
		return tree;
	}

	public Properties getProperties(IType type) {
		return tree.get(type);
	}

	private void load(IType type) {
		String postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".configure.postfix", "_Configure");
		try {
			Properties tmp = ResourceUtils.loadProperties(type.value() + postfix + ".properties");
			tree.put(type, tmp);
		} catch (BaseException e) {
			SystemUtils.println(System.err, new StringBuffer(e.getMessage()));
		}
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

		private final ConfigureContext context;

		private Singleton() {
			context = new ConfigureContext();
		}

		public ConfigureContext get() {
			return context;
		}
	}
}

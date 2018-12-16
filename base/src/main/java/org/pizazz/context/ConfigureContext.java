package org.pizazz.context;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.common.ResourceUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.ref.IType;
import org.pizazz.message.ref.TypeEnum;

/**
 * 内部配置环境组件
 * 
 * @author xlgp2171
 * @version 1.0.181216
 */
public final class ConfigureContext implements ICloseable {
	private final ConcurrentMap<IType, Properties> tree = new ConcurrentHashMap<IType, Properties>();

	public static final ConfigureContext getInstance() {
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
					tree.remove(type).clear();
				}
			}
		}
	}

	public Map<IType, Properties> getConfigureTree() {
		return tree;
	}

	private void load(IType type) {
		String _postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".configure.postfix", "_Configure");
		try {
			Properties _tmp = ResourceUtils.loadProperties(type.value() + _postfix + ".properties");
			tree.put(type, _tmp);
		} catch (BaseException e) {
			SystemUtils.println(System.err, new StringBuilder(e.getMessage()));
		}
	}

	@Override
	public void destroy(int timeout) throws BaseException {
		tree.values().stream().forEach(_item -> _item.clear());
		tree.clear();
	}

	private static enum Singleton {
		INSTANCE;

		private ConfigureContext context;

		private Singleton() {
			context = new ConfigureContext();
		}

		public ConfigureContext get() {
			return context;
		}
	}
}

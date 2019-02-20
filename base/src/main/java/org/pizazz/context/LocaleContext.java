package org.pizazz.context;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.common.SystemUtils;
import org.pizazz.message.TypeEnum;
import org.pizazz.message.ref.IType;

/**
 * 国际化消息环境组件
 * 
 * @author xlgp2171
 * @version 1.1.190220
 */
public final class LocaleContext implements ICloseable {
	private final ConcurrentMap<IType, Map<Locale, Properties>> tree;

	public static final LocaleContext getInstance() {
		return Singleton.INSTANCE.get();
	}

	public LocaleContext() {
		tree = new ConcurrentHashMap<IType, Map<Locale, Properties>>();
		register(TypeEnum.BASIC, SystemUtils.LOCAL_LOCALE);
	}

	public void register(IType type, Locale locale) {
		if (!tree.containsKey(type)) {
			synchronized (tree) {
				if (!tree.containsKey(type)) {
					ConcurrentMap<Locale, Properties> _tmp = new ConcurrentHashMap<Locale, Properties>();
					load(_tmp, type, locale);
					tree.put(type, _tmp);
				} else {
					Map<Locale, Properties> _tmp = tree.get(type);

					if (!_tmp.containsKey(locale)) {
						load(_tmp, type, locale);
					}
				}
			}
		} else {
			Map<Locale, Properties> _tmp = tree.get(type);

			if (_tmp != null && !_tmp.containsKey(locale)) {
				synchronized (_tmp) {
					if (!_tmp.containsKey(locale)) {
						load(_tmp, type, locale);
					}
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

	public Map<IType, Map<Locale, Properties>> getLocaleTree() {
		return tree;
	}

	public Properties getProperties(IType type, Locale locale) {
		Map<Locale, Properties> _tmp = tree.get(type);

		if (_tmp != null) {
			return _tmp.get(locale);
		}
		return null;
	}

	private void load(Map<Locale, Properties> item, IType type, Locale locale) {
		String _postfix = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".locale.postfix", "_Locale");
		ResourceBundle _resource = ResourceBundle.getBundle(type.value() + _postfix, locale);
		Properties _tmp = new Properties();
		_resource.keySet().stream().forEach(_item -> _tmp.setProperty(_item, _resource.getString(_item)));
		item.put(locale, _tmp);
	}

	@Override
	public void destroy(Duration timeout) {
		tree.values().stream().forEach(_item -> _item.clear());
		tree.clear();
	}

	private static enum Singleton {
		INSTANCE;

		private LocaleContext context;

		private Singleton() {
			context = new LocaleContext();
		}

		public LocaleContext get() {
			return context;
		}
	}
}

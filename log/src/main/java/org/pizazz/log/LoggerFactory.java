package org.pizazz.log;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pizazz.Constant;
import org.pizazz.IPlugin;
import org.pizazz.common.IOUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.log.exception.LogError;
import org.pizazz.log.record.RecordRunnable;
import org.pizazz.log.ref.ILoggerAdapter;
import org.pizazz.log.ref.LogEnum;
import org.pizazz.log.ref.TypeEnum;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ConfigureHelper;
import org.pizazz.message.LocaleHelper;
import org.pizazz.tool.AbstractClassPlugin;

/**
 * 日志输出器工厂
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class LoggerFactory {
	// 日志适配器
	private static volatile ILoggerAdapter ADAPTER;
	// 日志监控记录类
	private static final RecordRunnable RECORD;
	// 日志类 包含所有日志记录实现类
	private static final ConcurrentMap<String, Logger> LOGGERS;
	// 查找常用的日志框架
	static {
		// 获取日志记录类
		String _record = ConfigureHelper.getConfig(TypeEnum.LOG, Constant.NAMING_SHORT + ".logger.record", "DEF_RECORD",
				"");
		TupleObject _config = TupleObjectUtils.newObject(2).append("$CLASS", _record);

		if (!StringUtils.isTrimEmpty(_record)) {
			// 获取日志记录缓存最大值
			int _maxsize = ConfigureHelper.getConfig(TypeEnum.LOG, Constant.NAMING_SHORT + ".logger.record.size",
					"DEF_RECORD_SIZE", 1000);
			_config.put("$SIZE", _maxsize > 0 ? _maxsize : 1000);
		}
		// 初始化日志容器
		LOGGERS = new ConcurrentHashMap<String, Logger>();
		RECORD = new RecordRunnable();
		BaseException _e = null;
		try {
			RECORD.initialize(_config);
		} catch (BaseException e) {
			// 日志插件启用失败
			_e = e;
		}
		//
		LogClass _log = LogClass.newInstance();
		createAdapter(_log);
		Logger _logger = ADAPTER.getLogger("org.pizazz.log.LoggerFactory", RECORD);
		_logger.info(LocaleHelper.toLocaleText(TypeEnum.LOG, "PLUGIN.USE", ADAPTER.getId()));

		if (_e != null) {
			_logger.warn(_e.getMessage());
		} else {
			_logger.info(LocaleHelper.toLocaleText(TypeEnum.LOG, "RECORD.USE", RECORD.getId()));
		}
		waitFor(_log, _logger, 0);
	}

	private LoggerFactory() {
	}

	/**
	 * 构建日志输出器供给器
	 */
	static void createAdapter(AbstractClassPlugin factory) {
		// 获取日志适配类
		String _clazz = ConfigureHelper.getConfig(TypeEnum.LOG, Constant.NAMING_SHORT + ".logger.class", "DEF_CLASS",
				"org.pizazz.log.plugin.SimpleAdapter");
		// 获取日志配置路径
		String _resource = ConfigureHelper.getConfig(TypeEnum.LOG, Constant.NAMING_SHORT + ".logger.resource",
				"DEF_RESOURCE", "");
		TupleObject _config = TupleObjectUtils.newObject(2).append("$CLASS", _clazz).append("$RESOURCE", _resource);
		try {
			factory.initialize(_config);
		} catch (BaseException e) {
			IOUtils.close(RECORD, 0);
			throw new LogError(BasicCodeEnum.MSG_0019, e.getMessage(), e);
		}
		// for (String _item : LOGGERS.keySet()) {
		// LOGGERS.get(_item).setLogger(ADAPTER.getLogger(_item));
		// }
	}

	static void waitFor(AbstractClassPlugin factory, Logger logger, int timeout) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info(LocaleHelper.toLocaleText(TypeEnum.LOG, "PLUGIN.DESTROY", ADAPTER.getId()));
				LOGGERS.clear();
				IOUtils.close(RECORD, timeout);
				IOUtils.close(factory, timeout);
			}
		});
	}

	/**
	 * 获取日志输出器
	 * 
	 * @param key 分类键
	 * @return 日志输出器, 后验条件: 不返回null.
	 */
	public static Logger getLogger(Class<?> key) {
		String _clazz = key.getName();
		return getLogger(_clazz);
	}

	/**
	 * 获取日志输出器
	 * 
	 * @param key 分类键
	 * @return 日志输出器, 后验条件: 不返回null.
	 */
	public static Logger getLogger(String key) {
		Logger _logger = LOGGERS.get(key);

		if (_logger == null) {
			_logger = ADAPTER.getLogger(key, RECORD);
			LOGGERS.putIfAbsent(key, _logger);
		}
		return _logger;
	}

	/**
	 * 动态设置输出日志级别
	 * 
	 * @param level 日志级别
	 */
	public static void setLevel(LogEnum level) {
		ADAPTER.setLevel(level);
	}

	/**
	 * 获取日志级别
	 * 
	 * @return 日志级别
	 */
	public static LogEnum getLevel() {
		return ADAPTER.getLevel();
	}

	/**
	 * 获取日志文件
	 * 
	 * @return 日志文件
	 */
	public static Path getPath() {
		return ADAPTER.getPath();
	}

	private static class LogClass extends AbstractClassPlugin {
		public static LogClass newInstance() {
			return new LogClass();
		}

		@Override
		public void initialize(TupleObject config) throws BaseException {
			setConfig(config);
			// 首先加载simple logger
			// "org.pizazz.log.plugin.simple.SimpleAdapter"
			IPlugin _tmp = loadPlugin("$CLASS", null, null, true);
			try {
				ADAPTER = ILoggerAdapter.class.cast(_tmp);
			} catch (ClassCastException e) {
				throw new BaseException(
						LocaleHelper.toLocaleText(TypeEnum.LOG, "PLUGIN.CAST", ILoggerAdapter.class.getName()));
			}
		}

		@Override
		public void destroy(int timeout) {
			unloadPlugin(ADAPTER, timeout);
		}

		@Override
		protected void log(String msg, BaseException e) {
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
}

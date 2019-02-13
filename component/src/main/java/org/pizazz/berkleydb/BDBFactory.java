package org.pizazz.berkley;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;

import org.pizazz.IPlugin;
import org.pizazz.berkley.operator.Connection;
import org.pizazz.common.IOUtils;
import org.pizazz.common.ResourceUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class BDBFactory implements IPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(BDBFactory.class);
	private Environment environment = null;

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
		String _configPath = TupleObjectHelper.getString(config, BDBConstant.KEY_CONFIG_PATH, "");
		EnvironmentConfig _config = null;

		if (!StringUtils.isEmpty(_configPath)) {
			_config = new EnvironmentConfig(ResourceUtils.loadProperties(_configPath));
		} else {
			_config = new EnvironmentConfig();
		}
		_config.setAllowCreateVoid(true);
		_config.setReadOnlyVoid(
				TupleObjectHelper.getBoolean(config, BDBConstant.KEY_READ_ONLY, BDBConstant.DEF_READ_ONLY));
		File _envHome = new File(TupleObjectHelper.getString(config, BDBConstant.KEY_HOME_PATH, ""));

		if (!_envHome.isDirectory() || !_envHome.exists()) {
			throw new BaseException(BasicCodeEnum.MSG_0003, new FileNotFoundException(_envHome.getAbsolutePath()));
		}
		try {
			environment = new Environment(_envHome, _config);
		} catch (Exception e) {
			throw new BaseException(BasicCodeEnum.MSG_0021, e.getMessage(), e);
		}
		LOGGER.info("bdb initialized,config=" + config.toString());
	}

	public <E> Connection<E> getConnection(String name, DatabaseConfig config, Class<E> type) {
		Database _data = environment.openDatabase(null, name, config);
		Database _class = environment.openDatabase(null, name + BDBConstant.DEF_DB_CLASS, config);
		return new Connection<E>(_data, _class, type);
	}

	public <E> Connection<E> getConnection(String name, Class<E> type) {
		DatabaseConfig _config = new DatabaseConfig();
		_config.setAllowCreate(true);
		_config.setSortedDuplicatesVoid(false);
		_config.setReadOnlyVoid(false);
		_config.setDeferredWriteVoid(true);
		return getConnection(name, _config, type);
	}

	@Override
	public void destroy(Duration timeout) {
		if (environment != null) {
			try {
				environment.cleanLog();
			} catch (Exception e) {
			}
			IOUtils.close(environment);
		}
		LOGGER.info("bdb destroy,timeout=" + timeout);
	}
}

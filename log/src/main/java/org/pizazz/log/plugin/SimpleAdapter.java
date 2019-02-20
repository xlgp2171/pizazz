package org.pizazz.log.plugin;

import java.nio.file.Path;
import java.time.Duration;

import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.log.Logger;
import org.pizazz.log.record.RecordRunnable;
import org.pizazz.log.ref.ILoggerAdapter;
import org.pizazz.log.ref.LogEnum;

/**
 * 简单日志适配器组件
 * 
 * @author xlgp2171
 * @version 1.0.190220
 */
public class SimpleAdapter implements ILoggerAdapter {
	@Override
	public String getId() {
		return getClass().getName();
	}
	
	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	@Override
	public Logger getLogger(Class<?> key, RecordRunnable runnable) {
		return getLogger(key.getName(), runnable);
	}

	@Override
	public Logger getLogger(String key, RecordRunnable runnable) {
		return new SimpleLogger(key, runnable);
	}

	@Override
	public void setLevel(LogEnum level) {
	}

	@Override
	public LogEnum getLevel() {
		return LogEnum.OFF;
	}

	@Override
	public Path getPath() {
		return SystemUtils.LOCAL_DIR;
	}

	@Override
	public void destroy(Duration timeout) {
	}
}

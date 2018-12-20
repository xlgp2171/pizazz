package org.pizazz.log.plugin;

import java.time.Duration;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.log.AbstractLogger;
import org.pizazz.log.Logger;
import org.pizazz.log.exception.LogError;
import org.pizazz.log.record.RecordEntity;
import org.pizazz.log.record.RecordRunnable;
import org.pizazz.log.ref.LogEnum;

/**
 * 简单日志实现组件
 * 
 * @author xlgp2171
 * @version 1.0.181219
 */
public class SimpleLogger extends AbstractLogger implements Logger {

	public SimpleLogger(String clazz, RecordRunnable runnable) {
		super(clazz, runnable);
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	@Override
	public String filter(RecordEntity entity) throws LogError {
		return entity.toString();
	}

	@Override
	public void debug(String msg) {
		debug(msg, null);
	}

	@Override
	public void debug(String msg, Throwable e) {
		System.out.println(create(LogEnum.DEBUG, msg, e));

		if (e != null) {
			e.printStackTrace();
		}
	}

	@Override
	public void info(String msg) {
		info(msg, null);
	}

	@Override
	public void info(String msg, Throwable e) {
		System.out.println(create(LogEnum.INFO, msg, e));

		if (e != null) {
			e.printStackTrace();
		}
	}

	@Override
	public void warn(String msg) {
		warn(msg, null);
	}

	@Override
	public void warn(String msg, Throwable e) {
		System.err.println(create(LogEnum.WARN, msg, e));

		if (e != null) {
			e.printStackTrace();
		}
	}

	@Override
	public void error(String msg) {
		error(msg, null);
	}

	@Override
	public void error(String msg, Throwable e) {
		System.err.println(create(LogEnum.ERROR, msg, e));

		if (e != null) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
	}
}

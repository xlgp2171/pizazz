package org.pizazz.log;

import org.pizazz.log.exception.LogError;
import org.pizazz.log.record.ILoggerRecord;
import org.pizazz.log.record.RecordEntity;
import org.pizazz.log.record.RecordRunnable;
import org.pizazz.log.ref.LogEnum;

/**
 * 日志组件超类
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public abstract class AbstractLogger implements ILoggerRecord {
	private final String clazz;
	private final RecordRunnable runnable;

	/**
	 * 
	 * @param clazz 日志记录目标类
	 * @param runnable
	 */
	public AbstractLogger(String clazz, RecordRunnable runnable) {
		this.clazz = clazz;
		this.runnable = runnable;
	}

	@Override
	public String record(RecordEntity entity) throws LogError {
		runnable.push(entity);
		return filter(entity);
	}

	protected String create(LogEnum level, String msg, Throwable e) throws LogError {
		return record(new RecordEntity(level, clazz, msg, e));
	}

	@Override
	public String filter(RecordEntity entity) throws LogError {
		return runnable.filter(entity);
	}
}

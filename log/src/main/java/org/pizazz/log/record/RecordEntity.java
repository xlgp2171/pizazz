package org.pizazz.log.record;

import java.util.Date;

import org.pizazz.common.DateUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.log.ref.LogEnum;

/**
 * 日志消息实体
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class RecordEntity {
	/**
	 * 日志级别
	 */
	private final LogEnum level;
	/**
	 * 日志记录类
	 */
	private final String clazz;
	/**
	 * 日志信息
	 */
	private String msg;
	/**
	 * 异常信息
	 */
	private final Throwable throwable;
	/**
	 * 记录时间(认为类构建时间和日志记录时间相同)
	 */
	private final Date date;

	/**
	 * 日志记录信息类
	 * 
	 * @param level 日志级别
	 * @param clazz 日志记录类
	 * @param method 日志记录方法
	 * @param msg 日志信息
	 * @param throwable 异常信息
	 */
	public RecordEntity(LogEnum level, String clazz, String msg, Throwable throwable) {
		this.level = level;
		this.clazz = clazz;
		this.msg = msg;
		this.throwable = throwable;
		date = new Date();
	}

	public LogEnum getLevel() {
		return level;
	}

	public String getClassName() {
		return clazz;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return msg;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * 日志信息(重写toString方法)
	 * 
	 * @return 输出格式为yyyy-MM-dd HH:mm:ss[LEVEL](classpath)message
	 *         Exception:exceptionMsg
	 */
	@Override
	public String toString() {
		StringBuilder _tmp = new StringBuilder();
		try {
			_tmp.append(DateUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
		} catch (BaseException e) {
		}
		_tmp.append("[").append(level.name()).append("](").append(clazz).append(")").append(msg);

		if (throwable != null) {
			_tmp.append(" ").append(throwable.getClass().getSimpleName()).append(":").append(throwable.getMessage());
		}
		return _tmp.toString();
	}
}

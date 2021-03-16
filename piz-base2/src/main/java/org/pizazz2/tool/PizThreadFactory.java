package org.pizazz2.tool;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.pizazz2.PizContext;

/**
 * 线程工厂类<br>
 * 参考com.alibaba.dubbo.common.utils.NamedThreadFactory
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PizThreadFactory implements ThreadFactory {
	private final AtomicInteger threadNum = new AtomicInteger(1);
	private final String prefix;
	private final boolean daemon;
	private final UncaughtExceptionHandler handler;
	private final ThreadGroup group;

	public PizThreadFactory() {
		this(PizContext.NAMING_SHORT + "-pool", false, null);
	}

	public PizThreadFactory(String prefix, boolean daemon) {
		this(prefix, daemon, null);
	}

	public PizThreadFactory(String prefix, boolean daemon, UncaughtExceptionHandler handler) {
		this.prefix = prefix + "-thread-";
		this.daemon = daemon;
		this.handler = handler;
		SecurityManager manager = System.getSecurityManager();
		group = (manager == null) ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String name = prefix + threadNum.getAndIncrement();
		Thread tmp = new Thread(group, runnable, name, 0);
		tmp.setDaemon(daemon);

		if (handler != null) {
			tmp.setUncaughtExceptionHandler(handler);
		}
		return tmp;
	}

	public ThreadGroup getThreadGroup() {
		return group;
	}
}

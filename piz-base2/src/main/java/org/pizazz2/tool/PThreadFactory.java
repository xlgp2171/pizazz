package org.pizazz2.tool;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.pizazz2.Constant;

/**
 * 线程工厂类<br>
 * 参考com.alibaba.dubbo.common.utils.NamedThreadFactory
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PThreadFactory implements ThreadFactory {
	private final AtomicInteger threadNum = new AtomicInteger(1);
	private final String prefix;
	private final boolean daemon;
	private final UncaughtExceptionHandler handler;
	private final ThreadGroup group;

	public PThreadFactory() {
		this(Constant.NAMING_SHORT + "-pool", false, null);
	}

	public PThreadFactory(String prefix, boolean daemon) {
		this(prefix, daemon, null);
	}

	public PThreadFactory(String prefix, boolean daemon, UncaughtExceptionHandler handler) {
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

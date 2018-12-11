package org.pizazz.tool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂类<br>
 * 参考com.alibaba.dubbo.common.utils.NamedThreadFactory
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class PThreadFactory implements ThreadFactory {
	private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
	private final AtomicInteger threadNum = new AtomicInteger(1);
	private final String prefix;
	private final boolean daemon;
	private final ThreadGroup group;

	public PThreadFactory() {
		this("piz-pool-" + POOL_SEQ.getAndIncrement(), false);
	}

	public PThreadFactory(String prefix, boolean daemon) {
		this.prefix = prefix + "-thread-";
		this.daemon = daemon;
		SecurityManager _manager = System.getSecurityManager();
		group = (_manager == null) ? Thread.currentThread().getThreadGroup() : _manager.getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable runnable) {
		String _name = prefix + threadNum.getAndIncrement();
		Thread _tmp = new Thread(group, runnable, _name, 0);
		_tmp.setDaemon(daemon);
		return _tmp;
	}

	public ThreadGroup getThreadGroup() {
		return group;
	}
}

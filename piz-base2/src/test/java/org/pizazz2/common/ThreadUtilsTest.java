package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.tool.PizThreadFactory;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ThreadUtils测试
 *
 * @author xlgp2171
 * @version 2.0.211028
 */
public class ThreadUtilsTest {

	@Test
	public void testExecuteThread() throws ExecutionException, InterruptedException {
		AtomicBoolean sign = new AtomicBoolean(false);
		ThreadUtils.executeThread(() ->{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
			sign.compareAndSet(false, true);
		}).get();
		Assert.assertTrue(sign.get());
	}

	@Test
	public void testBlockingThread() throws IOException, InterruptedException {
		int parallelism = 3;
		ThreadPoolExecutor poolExecutor = ThreadUtils.newThreadPool(parallelism, new LinkedBlockingQueue<>(parallelism),
				new PizThreadFactory("BlockingThread", true));
		poolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		for (int i = 0; i < 10; i ++) {
			final int index = i;
			poolExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(NumberUtils.random(1000, 3000));
						System.out.println(Thread.currentThread().getName() + ":" + index);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			});
		}
		System.out.println("finish");
		poolExecutor.shutdown();

		if (poolExecutor.awaitTermination(100, TimeUnit.SECONDS)) {
			// do nothing
		}

		System.out.println("complete");
	}

	public static class StopPolicy implements RejectedExecutionHandler {
		public StopPolicy() { }

		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			if (!e.isShutdown()) {
				try {
					e.getQueue().put(r);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}

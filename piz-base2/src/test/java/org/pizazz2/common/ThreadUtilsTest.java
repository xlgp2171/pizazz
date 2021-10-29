package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
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
}

package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.IMessageOutput;
import org.pizazz2.exception.BaseException;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ShellFactory测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ShellFactoryTest {

	@Test
	public void testCommand() throws BaseException, ExecutionException, InterruptedException {
		ShellBuilder builder = ShellFactory.newInstance("ping", "127.0.0.1")
				// 直到命令执行完成
				.waitFor(Duration.ofMillis(-1))
				.charset(Charset.forName("GB2312"));
		List<String> result = builder.execute(IMessageOutput.EMPTY_STRING).get();
		Assert.assertEquals(result.size(), 5, 20);
	}
}

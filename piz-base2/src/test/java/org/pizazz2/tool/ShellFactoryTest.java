package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.IMessageOutput;
import org.pizazz2.exception.BaseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
				.waitFor(Duration.ofMillis(0))
				.charset(Charset.forName("GB2312"));
		List<String> result = builder.execute(IMessageOutput.EMPTY_STRING).get();
		result.forEach(System.out::println);
		Assert.assertEquals(result.size(), 5, 20);
	}

	@Test
	public void testCommand2() throws BaseException {
		String[] command = {"E:/Platform/Laboratory/space01/ffmpeg/bin/ffmpeg.exe", "-i",
				"E:/Platform/Laboratory/space01/1.avi", "E:/Platform/Laboratory/space01/1.mp4"};

		ShellBuilder builder = ShellFactory.newInstance(command)
				.waitFor(Duration.ofSeconds(0))
				;
		builder.execute(new IMessageOutput<String>() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public void write(String message) {
				System.out.println(message);
			}
		});
		System.out.println("FINISHED");
	}


	public static void main(String[] args) throws IOException {
		ProcessBuilder builder = new ProcessBuilder().command("cmd", "/c", "ping 127.0.0.1");
		builder.redirectErrorStream(true);
		Process process = builder.start();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), "GB2312"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
		process.destroy();
	}



}

package org.pizazz2.tool.ref;

import org.pizazz2.IMessageOutput;
import org.pizazz2.exception.BaseException;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * SHELL工厂接口
 * 
 * @author xlgp2171
 * @version 2.1.210720
 */
public interface IShellFactory {

	/**
	 * 新创建进程
	 * @param builder 进程构造器
	 * @param timeout 超时时间
	 * @param function 处理process
	 * @return 任意类型
	 * @throws BaseException 构建异常
	 */
	<T>T newProcess(ProcessBuilder builder, Duration timeout,
					   Function<Process, T> function) throws BaseException;

	/**
	 * 输出方式匹配
	 * @param in 输入流
	 * @param charset 解析字符流
	 * @param output 输出内容
	 * @return 封装实体
	 */
	CompletableFuture<List<String>> apply(InputStream in, Charset charset, IMessageOutput<String> output);

	/**
	 * 获取线程池
	 * @return 线程池
	 */
	ThreadPoolExecutor getThreadPool();
}

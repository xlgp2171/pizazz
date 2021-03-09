package org.pizazz2.tool.ref;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.pizazz2.IMessageOutput;
import org.pizazz2.exception.BaseException;

/**
 * SHELL工厂接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IShellFactory {

	/**
	 * 新创建进程
	 * @param builder 进程构造器
	 * @param timeout 超时时间
	 * @return 进程
	 * @throws BaseException 构建异常
	 */
	Process newProcess(ProcessBuilder builder, Duration timeout) throws BaseException;

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

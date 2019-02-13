package org.pizazz.tool.ref;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.pizazz.IMessageOutput;
import org.pizazz.exception.BaseException;

/**
 * SHELL工厂接口
 * 
 * @author xlgp2171
 * @version 1.2.190213
 */
public interface IShellFactory {

	public Process newProcess(ProcessBuilder builder, Duration timeout) throws BaseException;

	public CompletableFuture<List<String>> apply(InputStream in, Charset charset, IMessageOutput<String> output);

	public ExecutorService getExecutorService();
}

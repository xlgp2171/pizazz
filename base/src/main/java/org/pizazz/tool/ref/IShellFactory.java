package org.pizazz.tool.ref;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.exception.BaseException;

/**
 * SHELL工厂接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IShellFactory {

	public Process newProcess(ProcessBuilder builder, int timeout) throws BaseException;

	public Future<List<String>> submit(InputStream in, Charset charset, ICloseable shutdown,
			IMessageOutput<String> output);
}

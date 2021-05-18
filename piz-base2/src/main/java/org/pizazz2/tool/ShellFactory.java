package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.IMessageOutput;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.ConfigureHelper;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.tool.ref.IShellFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * SHELL工厂组件
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public final class ShellFactory implements IShellFactory, ICloseable {
    public static final int POOL_SIZE = 8;
    private final ThreadPoolExecutor threadPool;

    public ShellFactory() {
        int poolSize = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_SHELL_POOL_MAX",
                Runtime.getRuntime().availableProcessors());
        poolSize = poolSize > 0 ? Math.min(poolSize, POOL_SIZE) : POOL_SIZE;
        threadPool = ThreadUtils.newDaemonThreadPool(poolSize, "-shell");
    }

    public static ShellBuilder newInstance(String... command) throws ValidateException {
        return new ShellBuilder(Singleton.INSTANCE.get(), command);
    }

    @Override
    public Process newProcess(ProcessBuilder builder, Duration timeout) throws BaseException {
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.START", e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
        }
        try {
            if (timeout.isZero()) {
                process.waitFor();
            } else if (!timeout.isNegative()) {
                process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PROCESS.WAIT", e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
        }
        return process;
    }

    @Override
    public CompletableFuture<List<String>> apply(InputStream in, Charset charset, IMessageOutput<String> output) {
        return CompletableFuture.supplyAsync(new StreamSupplier(in, charset, output == null ?
                IMessageOutput.EMPTY_STRING : output), threadPool);
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    @Override
    public void destroy(Duration timeout) {
        ThreadUtils.shutdown(threadPool, timeout);
    }

    private static class StreamSupplier implements Supplier<List<String>> {
        private final InputStream in;
        private final Charset charset;
        private final IMessageOutput<String> call;

        public StreamSupplier(InputStream in, Charset charset, IMessageOutput<String> call) {
            this.in = in;
            this.charset = charset;
            this.call = call;
        }

        @Override
        public List<String> get() {
            final List<String> tmp = new LinkedList<>();
            IOUtils.readLine(in, charset, new IMessageOutput<String>() {
                @Override
                public void write(String message) {
                    if (call.isEnabled()) {
                        call.write(message);
                    }
                    tmp.add(message);
                }

                @Override
                public void throwException(Exception e) {
                    call.throwException(e);
                }
            });
            return tmp;
        }
    }

    public enum Singleton {
        /**
         * 单例
         */
        INSTANCE;

        private final ShellFactory factory;

        Singleton() {
            factory = new ShellFactory();
        }

        public ShellFactory get() {
            return factory;
        }
    }
}

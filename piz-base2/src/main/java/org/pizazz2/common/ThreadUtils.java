package org.pizazz2.common;

import org.pizazz2.PizContext;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.tool.PizThreadFactory;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * 线程工具
 *
 * @author xlgp2171
 * @version 2.1.220707
 */
public class ThreadUtils {

    public static ThreadPoolExecutor newThreadPool(
            int poolSize, BlockingQueue<Runnable> workQueue, PizThreadFactory factory,
            RejectedExecutionHandler handler) throws ValidateException {
        ValidateUtils.limit("newThreadPool", 1, poolSize, 0, Short.MAX_VALUE);
        ValidateUtils.notNull("newThreadPool", 1, workQueue, factory, handler);
        return new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, workQueue, factory,
                handler);
    }

    public static ThreadPoolExecutor newThreadPool(int poolSize, BlockingQueue<Runnable> workQueue,
                                                   PizThreadFactory factory) throws ValidateException {
        return ThreadUtils.newThreadPool(poolSize, workQueue, factory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(int poolSize, PizThreadFactory factory)
            throws ValidateException {
        ValidateUtils.limit("newScheduledThreadPool", 1, poolSize, 0, Short.MAX_VALUE);
        ValidateUtils.notNull("newScheduledThreadPool", 1, factory);
        return new ScheduledThreadPoolExecutor(poolSize, factory);
    }

    public static ThreadPoolExecutor newThreadPool(int poolSize, PizThreadFactory factory) throws ValidateException {
        return ThreadUtils.newThreadPool(poolSize, new LinkedBlockingQueue<>(), factory);
    }

    public static ThreadPoolExecutor newDaemonThreadPool(int poolSize, String name) throws ValidateException {
        name = StringUtils.isTrimEmpty(name) ? SystemUtils.newUUIDSimple() : name;
        return ThreadUtils.newThreadPool(poolSize, new PizThreadFactory(PizContext.NAMING_SHORT + "-" + name,
                true));
    }

    public static ThreadPoolExecutor newDaemonBlockingThreadPool(int poolSize, String name) throws ValidateException {
        return ThreadUtils.newThreadPool(poolSize, new LinkedBlockingQueue<>(poolSize),
                new PizThreadFactory(StringUtils.nullToEmpty(name), true),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ScheduledThreadPoolExecutor newDaemonScheduledThreadPool(int poolSize, String name)
            throws ValidateException {
        name = StringUtils.isTrimEmpty(name) ? SystemUtils.newUUIDSimple() : name;
        return ThreadUtils.newScheduledThreadPool(poolSize, new PizThreadFactory(PizContext.NAMING_SHORT + "-" +
                name, true));
    }

    public static ThreadPoolExecutor newSingleThreadPool() {
        return ThreadUtils.newThreadPool(1, new PizThreadFactory(PizContext.NAMING_SHORT + "-single",
                true));
    }

    /**
     * 运行一个线程
     * @param runnable 线程内运行实现
     * @return Future对象
     */
    public static CompletableFuture<Void> executeThread(Runnable runnable) {
        ThreadPoolExecutor executor = ThreadUtils.newSingleThreadPool();
        return CompletableFuture.runAsync(runnable, executor)
                .whenComplete((item, throwable) -> executor.shutdownNow());
    }

    /**
     * 安全关闭线程池
     *
     * @param service 线程池对象
     * @param timeout 超时时间
     */
    public static void shutdown(ExecutorService service, Duration timeout) {
        // 线程池异常完成和正常关闭
        if (service == null || service.isTerminated() || service.isShutdown()) {
            return;
        }
        // 当有设置有超时时间
        if (timeout != null && !timeout.isNegative() && !timeout.isZero()) {
            // 先尝试关闭
            service.shutdown();
            try {
                // 若在等待时间内关闭
                if (service.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        // 强行关闭所有线程
        service.shutdownNow();
    }
}

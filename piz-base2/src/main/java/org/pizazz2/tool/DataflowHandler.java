package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.tool.ref.IDataflowListener;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * 流式处理操作<br>
 * 参考elasticsearch
 *
 * @author xlgp2171
 * @version 2.0.210827
 *
 * @param <T> 元数据
 */
public class DataflowHandler<T> implements ICloseable {
    protected final BiConsumer<Collection<T>, TupleObject> consumer;
    protected final IDataflowListener<T> listener;
    protected final TupleObject config;
    protected final boolean sync;
    protected final int threads;
    protected final Semaphore semaphore;
    protected final ThreadPoolExecutor executor;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DataflowHandler(BiConsumer<Collection<T>, TupleObject> consumer, IDataflowListener<T> listener,
                           TupleObject config, boolean sync, int threads) {
        this.consumer = consumer;
        this.listener = listener;
        this.config = config;
        this.sync = sync;
        this.threads = threads;
        int tmp = Math.max(threads, 1);
        this.semaphore = new Semaphore(tmp);
        executor = ThreadUtils.newDaemonThreadPool(tmp, "-dataflow-execute");
    }

    protected Runnable toRunnable(long executionId, Collection<T> data) {
        return () -> {
            try {
                if (!isClosed()) {
                    consumer.accept(data, config);
                    listener.after(executionId, data);
                }
            } catch (RuntimeException e) {
                listener.exception(executionId, data, e);
            } finally {
                semaphore.release();
            }
        };
    }

    protected void executeRunnable(Runnable runnable) {
        if (runnable != null) {
            // 同步执行
            if (sync) {
                runnable.run();
            } else {
                executor.execute(runnable);
            }
        }
    }

    public void execute(long executionId, Collection<T> data) {
        Runnable toRelease = () -> {};
        boolean successful = false;
        try {
            listener.before(executionId, data);
            // 只允许单一处理进行通过
            semaphore.acquire();
            toRelease = semaphore::release;
            Runnable runnable = toRunnable(executionId, data);
            executeRunnable(runnable);
            successful = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.exception(executionId, data, e);
        } catch (Exception e) {
            listener.exception(executionId, data, e);
        } finally {
            if (!successful) {
                toRelease.run();
            }
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void destroy(Duration timeout) {
        if (closed.compareAndSet(false, true)) {
            if (timeout != null && !timeout.isNegative() && !timeout.isZero()) {
                try {
                    if (semaphore.tryAcquire(threads, 1L, TimeUnit.MILLISECONDS)) {
                        semaphore.release(threads);
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            ThreadUtils.shutdown(executor, timeout);
        }
    }
}

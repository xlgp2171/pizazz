package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.PizContext;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.tool.ref.BatchedData;
import org.pizazz2.tool.ref.IData;
import org.pizazz2.tool.ref.IDataflowListener;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * 流式处理器<br>
 * 参考elasticsearch
 *
 * @author xlgp2171
 * @version 2.1.211014
 *
 * @param <E> 元数据
 */
public class DataflowProcessor<E extends IData> implements ICloseable {
    private final DataflowHandler<E> handler;
    private final int actions;
    private final long size;
    private final long interval;
    private final BatchedData<E> batchedData;
    private final ScheduledThreadPoolExecutor scheduler;

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicLong executionIdGen = new AtomicLong();

    DataflowProcessor(BiConsumer<Collection<E>, TupleObject> consumer, IDataflowListener<E> listener,
                      DataflowHandler<E> handler, TupleObject config, boolean sync, int threads, int actions,
                      long size, long interval, boolean daemon) throws ValidateException {
        if (actions <= 0 && size <= 0 && interval <= 0) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.MULTI", "DataflowProcessor");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
        if (handler == null) {
            listener = new IDataflowListener.ProxyListener<>(listener);
            handler = new DataflowHandler<>(consumer, listener, config, sync, threads);
        }
        this.handler = handler;
        this.actions = actions;
        this.size = size;
        this.interval = interval;
        // 若小于10个就10个
        batchedData = new BatchedData<>(Math.max(actions, 10));
        // 定时任务线程池
        scheduler = ThreadUtils.newScheduledThreadPool(NumberUtils.ONE.intValue(),
                new PizThreadFactory(PizContext.NAMING_SHORT + "-dataflow", daemon));
        scheduler.setRemoveOnCancelPolicy(true);
        // 按照设定时间启动任务
        startFlushTask();
    }

    /**
     * 构建数据流处理器
     *
     * @param consumer 数据处理接口
     * @param listener 数据监听接口
     * @param <E> 实现IData接口
     * @return 流处理器构建器
     */
    public static <E extends IData> Builder<E> builder(
            BiConsumer<Collection<E>, TupleObject> consumer, IDataflowListener<E> listener) {
        return new Builder<>(consumer, listener);
    }

    /**
     * 构建数据流处理器
     *
     * @param consumer 数据处理接口
     * @param <E> 实现IData接口
     * @return 流处理器构建器
     */
    public static <E extends IData> Builder<E> builder(BiConsumer<Collection<E>, TupleObject> consumer) {
        return DataflowProcessor.builder(consumer, null);
    }

    public void add(E object) throws ValidateException {
        ValidateUtils.notNull("add", object);
        internalAdd(object);
    }

    private synchronized void internalAdd(E object) {
        if (!isClosed()) {
            batchedData.add(object);

            if (isOverTheLimit()) {
                execute();
            }
        }
    }

    private boolean isOverTheLimit() {
        if (actions != NumberUtils.NEGATIVE_ONE.intValue() && batchedData.total() >= actions) {
            return true;
        } else {
            return size != NumberUtils.NEGATIVE_ONE.intValue() && batchedData.bytes() >= size;
        }
    }

    private void startFlushTask() {
        if (interval > 0) {
            scheduler.schedule(new Flush(), interval, TimeUnit.MILLISECONDS);
        }
    }

    private void execute() {
        this.handler.execute(executionIdGen.incrementAndGet(), batchedData.reset());
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void destroy(Duration timeout) {
        if (closed.compareAndSet(false, true)) {
            // 将余下缓存数据提交
            execute();
            SystemUtils.destroy(handler, timeout);
            ThreadUtils.shutdown(scheduler, null);
        }
    }

    public static class Builder<E extends IData> {
        private final BiConsumer<Collection<E>, TupleObject> consumer;
        private final IDataflowListener<E> listener;
        /** 同步执行 */
        private boolean sync = false;
        /** 运行线程数 */
        private int threads = NumberUtils.ONE.intValue();
        /** 触发个数(个) */
        private int actions = 20;
        /** 触发大小(byte) */
        private long size = 1024 * 1024 * 10;
        /** 触发周期(ms) */
        private long interval = 1000;
        /** 线程池是否守护线程 */
        private boolean daemon = true;

        private DataflowHandler<E> handler;
        private TupleObject config;

        Builder(BiConsumer<Collection<E>, TupleObject> consumer, IDataflowListener<E> listener) {
            this.consumer = consumer;
            this.listener = listener;
        }

        /**
         * 数据同步处理
         *
         * @param sync 是否同步运行
         * @return 当前对象
         */
        public Builder<E> setSync(boolean sync) {
            this.sync = sync;
            return this;
        }

        /**
         * 数据处理线程数
         *
         * @param threads 线程数
         * @return 当前对象
         */
        public Builder<E> setThreads(int threads) {
            this.threads = threads;
            return this;
        }

        /**
         * 数据流发送量阈值
         *
         * @param actions 发送量阈值
         * @return 当前对象
         */
        public Builder<E> setActions(int actions) {
            if (actions > 0) {
                this.actions = actions;
            }
            return this;
        }

        /**
         * 数据流发送大小阈值
         *
         * @param size 发送大小阈值
         * @return 当前对象
         */
        public Builder<E> setSize(long size) {
            if (size > 0) {
                this.size = size;
            }
            return this;
        }

        /**
         * 数据流发送时限阈值
         *
         * @param interval 发送时限阈值
         * @return 当前对象
         */
        public Builder<E> setInterval(long interval) {
            if (interval > 0) {
                this.interval = interval;
            }
            return this;
        }

        /**
         * 流式处理操作实现
         *
         * @param handler 操作实现
         * @return 当前对象
         */
        public Builder<E> setHandler(DataflowHandler<E> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<E> setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public Builder<E> setConfig(TupleObject config) {
            this.config = config;
            return this;
        }

        /**
         * 通过配置构建
         *
         * @return 流式处理器实现
         */
        public DataflowProcessor<E> build() {
            return new DataflowProcessor<>(
                    consumer, listener, handler, config, sync, threads, actions, size, interval, daemon);
        }
    }

    class Flush implements Runnable {
        @Override
        public void run() {
            synchronized (DataflowProcessor.this) {
                if (isClosed()) {
                    return;
                }
                try {
                    if (batchedData.total() > 0) {
                        execute();
                    }
                } finally {
                    scheduler.schedule(Flush.this, interval, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}

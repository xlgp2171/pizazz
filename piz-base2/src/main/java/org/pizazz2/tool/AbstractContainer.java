package org.pizazz2.tool;

import java.time.Duration;
import java.util.concurrent.*;

import org.pizazz2.*;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.helper.ConfigureHelper;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.tool.ref.ContainerStatusEnum;


/**
 * 容器组件超类
 *
 * @param <T> 输出类型
 * @author xlgp2171
 * @version 2.0.210201
 */
public abstract class AbstractContainer<T> implements IPlugin {
    public static final String CONTAINER_TIMEOUT = "timeout";

    protected final TupleObject properties;
    protected final IRunnable runnable;
    protected final IMessageOutput<T> output;



	/**
	 * 日志记录及异常抛出
	 * @param msg 日志消息
	 * @param exception 异常
	 */
	protected abstract void log(String msg, Exception exception);

    public AbstractContainer(IRunnable runnable, IMessageOutput<T> output) throws ValidateException {
        ValidateUtils.notNull("AbstractContainer", runnable, output);
        properties = TupleObjectHelper.newObject(4);
        this.runnable = runnable;
        this.output = output;
    }

    public void waitForShutdown() throws BaseException {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONTAINER.ALIVE", e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0012, msg, e);
        }
    }

    @Override
    public void initialize(IObject config) throws ToolException {
        properties.append(CONTAINER_TIMEOUT, ConfigureHelper.getConfig(TypeEnum.BASIC,
				PizContext.NAMING_SHORT + ".sc." + CONTAINER_TIMEOUT, "DEF_CONTAINER_TIMEOUT", "30000"));
    }

    @Override
    public void destroy(Duration timeout) {
        final Callable<ContainerStatusEnum> callable = () -> {
            try {
                AbstractContainer.this.runnable.destroy(Duration.ZERO);
            } catch (Exception e) {
                AbstractContainer.this.output.throwException(e);
                return ContainerStatusEnum.DESTROY_ERROR;
            }
            return ContainerStatusEnum.DESTROYED;
        };
		ContainerStatusEnum status = ContainerStatusEnum.DESTROYED;

        if (timeout == null || timeout.isNegative()) {
        	// 默认最大销毁超时时间
            int maxTimeout = ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_CONTAINER_TIMEOUT_MAX", 60000);
            // 手动设置的销毁超时时间
            int exitTime = TupleObjectHelper.getInt(properties, CONTAINER_TIMEOUT, 20000);
            // 销毁超时时间以最小时间为准
            timeout = Duration.ofMillis((exitTime > 0 && exitTime <= maxTimeout) ? exitTime : maxTimeout);
        }
        // 超时时间为0 立即销毁
        if (timeout.isZero()) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.DESTROY");
            SystemUtils.println(System.out, new StringBuffer(msg));
            log(msg, null);
            try {
                status = callable.call();
            } catch (Exception e) {
                output.throwException(e);
            }
        } else {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "CONTAINER.DESTROY.TIMEOUT", timeout.toMillis());
            SystemUtils.println(System.out, new StringBuffer(msg));
            log(msg, null);
            ScheduledThreadPoolExecutor pool = null;
            try {
                pool = new ScheduledThreadPoolExecutor(1, new PizThreadFactory());
                status = pool.submit(callable).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONTAINER.TIMEOUT", timeout);
                SystemUtils.println(System.err, new StringBuffer(msg));
                log(msg, e);
                status = ContainerStatusEnum.DESTROY_TIMEOUT;
            } catch (Exception e) {
                status = ContainerStatusEnum.THREAD_ERROR;
                output.throwException(e);
                e.printStackTrace();
                Runtime.getRuntime().halt(status.getStatus());
            } finally {
                SystemUtils.destroy(output, Duration.ZERO);

                if (pool != null) {
                    pool.shutdownNow();
                }
            }
        }
        if (status != ContainerStatusEnum.THREAD_ERROR) {
            Runtime.getRuntime().exit(status.getStatus());
        }
    }
}

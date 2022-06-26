package org.pizazz2.kafka.consumer.adapter;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.pizazz2.common.JSONUtils;
import org.pizazz2.common.ThreadUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz2.kafka.consumer.ConsumerModeEnum;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fork适配器
 *
 * @author xlgp2171
 * @version 2.1.220626
 */
public class ForkPoolAdapter implements IProcessAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForkPoolAdapter.class);
	private ForkJoinPool pool;
	private ConsumerModeEnum mode;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		int threads = Runtime.getRuntime().availableProcessors();
		threads = TupleObjectHelper.getInt(config, KafkaConstant.KEY_THREADS, threads);
		pool = new ForkJoinPool(threads);
		LOGGER.info(KafkaConstant.LOG_TAG + "adapter ForkPoolAdapter initialized,config=" + config);
	}

	@Override
	public void setMode(ConsumerModeEnum mode) throws KafkaException {
		switch (mode) {
		case AUTO_ASYNC_ROUND:
		case MANUAL_ASYNC_EACH:
		case MANUAL_SYNC_EACH:
			break;
		default:
			throw new KafkaException(CodeEnum.KFK_0001, "adapter not support:" + mode);
		}
		this.mode = mode;
	}

	@Override
	public void accept(IBridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException {
		ForkJoinTask<?> task = ForkJoinTask.adapt(() -> {
			try {
				bridge.passing();

				if (KafkaConstant.DEBUG_MODE) {
					LOGGER.debug(KafkaConstant.LOG_TAG + "consume:" + bridge.getId());
				}
			} catch (Exception e) {
				LOGGER.error(KafkaConstant.LOG_TAG + "consume:" + bridge.getId(), e);
			}
		});
		pool.execute(task);
	}

	@Override
	public String report() {
		TupleObject tmp = TupleObjectHelper.newObject("STATUS", "ALIVE").append("MODE", mode).append("ADAPTER",
				getClass().getName());

		if (pool != null) {
			tmp.append("STATUS", (pool.isShutdown() || pool.isTerminated()) ? "SHUTDOWN" : "ALIVE").append("INFO",
					TupleObjectHelper.newObject("ACTIVE_THREAD", pool.getActiveThreadCount())
							.append("RUNNING_THREAD", pool.getRunningThreadCount())
							.append("QUEUED_SUBMISSION", pool.getQueuedSubmissionCount())
							.append("QUEUED_TASK", pool.getQueuedTaskCount()));
		}
		try {
			return JSONUtils.toJSON(tmp);
		} catch (Exception e) {
			return tmp.toString();
		}
	}

	@Override
	public void destroy(Duration timeout) {
		ThreadUtils.shutdown(pool, timeout);
		LOGGER.info(KafkaConstant.LOG_TAG + "adapter ForkPoolAdapter destroyed,timeout=" + timeout);
	}
}

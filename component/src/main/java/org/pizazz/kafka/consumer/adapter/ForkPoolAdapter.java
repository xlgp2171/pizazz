package org.pizazz.kafka.consumer.adapter;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.pizazz.common.JSONUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.KafkaConstant;
import org.pizazz.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz.kafka.consumer.ConsumerModeEnum;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForkPoolAdapter implements IProcessAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForkPoolAdapter.class);
	private ForkJoinPool pool;
	private ConsumerModeEnum mode;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		pool = new ForkJoinPool(TupleObjectHelper.getInt(config, KafkaConstant.KEY_THREADS,
				Runtime.getRuntime().availableProcessors()));
		LOGGER.info("adapter ForkPoolAdapter initialized,config=" + config);
	}

	@Override
	public void set(ConsumerModeEnum mode) throws KafkaException {
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
	public void accept(Bridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException {
		ForkJoinTask<?> _task = ForkJoinTask.adapt(new Runnable() {
			@Override
			public void run() {
				try {
					bridge.passing();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("consume:" + bridge.getId());
					}
				} catch (Exception e) {
					LOGGER.error("consume:" + bridge.getId(), e);
				}
			}
		});
		pool.submit(_task);
	}

	@Override
	public String monitor() {
		TupleObject _tmp = TupleObjectHelper.newObject("STATUS", "deactivate").append("MODE", mode).append("ADAPTER",
				getClass().getName());

		if (pool != null) {
			_tmp.append("STATUS", (pool.isShutdown() || pool.isTerminated()) ? "shutdown" : "activate").append("INFO",
					TupleObjectHelper.newObject("ACTIVE_THREAD", pool.getActiveThreadCount())
							.append("RUNNING_THREAD", pool.getRunningThreadCount())
							.append("QUEUED_SUBMISSION", pool.getQueuedSubmissionCount())
							.append("QUEUED_TASK", pool.getQueuedTaskCount()));
		}
		try {
			return JSONUtils.toJSON(_tmp);
		} catch (Exception e) {
			return _tmp.toString();
		}
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (timeout == null || timeout.isNegative() || timeout.isZero()) {
			pool.shutdownNow();
		} else {
			pool.shutdown();
		}
		LOGGER.info("adapter ForkPoolAdapter destroyed,timeout=" + timeout);
	}

}

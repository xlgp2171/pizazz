package org.pizazz.kafka.consumer.adapter;

import java.time.Duration;

import org.pizazz.common.JSONUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz.kafka.consumer.ConsumerModeEnum;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceAdapter implements IProcessAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAdapter.class);
	private ConsumerModeEnum mode;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		LOGGER.info("adapter SequenceAdapter initialized,config=" + config);
	}

	@Override
	public void set(ConsumerModeEnum mode) throws KafkaException {
		switch (mode) {
		case AUTO_ASYNC_ROUND:
		case MANUAL_ASYNC_ROUND:
		case MANUAL_ASYNC_EACH:
		case MANUAL_SYNC_ROUND:
		case MANUAL_SYNC_EACH:
			break;
		default:
			throw new KafkaException(CodeEnum.KFK_0001, "adapter not support:" + mode);
		}
		this.mode = mode;
	}

	@Override
	public void accept(Bridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException {
		try {
			bridge.passing();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consume:" + bridge.getId());
			}
		} catch (Exception e) {
			if (ignore.consumeThrowable()) {
				if (e instanceof KafkaException) {
					throw (KafkaException) e;
				}
				throw new KafkaException(null, "consume:" + bridge.getId(), e);
			} else {
				LOGGER.warn("consume:" + bridge.getId(), e);
			}
		}
	}

	@Override
	public String monitor() {
		TupleObject _tmp = TupleObjectHelper.newObject("STATUS", "activate").append("MODE", mode).append("ADAPTER",
				getClass().getName());
		try {
			return JSONUtils.toJSON(_tmp);
		} catch (Exception e) {
			return _tmp.toString();
		}
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		LOGGER.info("adapter SequenceAdapter destroyed,timeout=" + timeout);
	}
}

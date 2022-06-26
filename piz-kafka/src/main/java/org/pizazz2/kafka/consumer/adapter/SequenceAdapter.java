package org.pizazz2.kafka.consumer.adapter;

import java.time.Duration;

import org.pizazz2.IObject;
import org.pizazz2.common.JSONUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz2.kafka.consumer.ConsumerModeEnum;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 顺序适配器
 *
 * @author xlgp2171
 * @version 2.1.220626
 */
public class SequenceAdapter implements IProcessAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAdapter.class);
	private ConsumerModeEnum mode;

	@Override
	public void initialize(TupleObject config) throws KafkaException {
		LOGGER.info(KafkaConstant.LOG_TAG + "adapter SequenceAdapter initialized,config=" + config);
	}

	@Override
	public void setMode(ConsumerModeEnum mode) throws KafkaException {
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
	public void accept(IBridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException {
		try {
			bridge.passing();

			if (KafkaConstant.DEBUG_MODE) {
				LOGGER.debug(KafkaConstant.LOG_TAG + "consume:" + bridge.getId());
			}
		} catch (Exception e) {
			if (ignore.consumeThrowable()) {
				if (e instanceof KafkaException) {
					throw (KafkaException) e;
				}
				throw new KafkaException(CodeEnum.KFK_0010, "consume:" + bridge.getId(), e);
			} else {
				LOGGER.warn(KafkaConstant.LOG_TAG + "consume:" + bridge.getId(), e);
			}
		}
	}

	@Override
	public String report() {
		TupleObject tmp = TupleObjectHelper.newObject("STATUS", "ALIVE").append("MODE", mode).append("ADAPTER",
				getClass().getName());
		try {
			return JSONUtils.toJSON(tmp);
		} catch (Exception e) {
			return tmp.toString();
		}
	}

	@Override
	public void destroy(Duration timeout) {
		LOGGER.info(KafkaConstant.LOG_TAG + "adapter SequenceAdapter destroyed,timeout=" + timeout);
	}
}

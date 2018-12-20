package org.pizazz.kafka.consumer.adapter;

import java.time.Duration;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.consumer.ConsumerModeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderLoopAdapter implements IProcessAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderLoopAdapter.class);

	@Override
	public void initialize(TupleObject config) throws BaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(ConsumerModeEnum mode) throws KafkaException {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(Bridge bridge) throws KafkaException {
		// TODO Auto-generated method stub

	}

	@Override
	public String monitor() {
		// TODO Auto-generated method stub
		return null;
	}

}

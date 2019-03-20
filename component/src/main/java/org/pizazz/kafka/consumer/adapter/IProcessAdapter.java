package org.pizazz.kafka.consumer.adapter;

import org.pizazz.IPlugin;
import org.pizazz.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz.kafka.consumer.ConsumerModeEnum;
import org.pizazz.kafka.exception.KafkaException;

public interface IProcessAdapter extends IPlugin {

	public void set(ConsumerModeEnum mode) throws KafkaException;

	public void accept(IBridge bridge, ConsumerIgnoreEnum ignore) throws KafkaException;

	public String monitor();
}

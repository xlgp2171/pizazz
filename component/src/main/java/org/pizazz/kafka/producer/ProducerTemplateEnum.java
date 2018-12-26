package org.pizazz.kafka.producer;

import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.common.YAMLUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.KafkaConstant;

public enum ProducerTemplateEnum {
	/** 高效 */
	PRODUCER_EFFICIENCY,
	/** 标准 */
	PRODUCER_NORMAL,
	/** 事务 */
	CONSUMER_TRANSACTION,
	/** 无 */
	NONE;

	public void fill(TupleObject clientC, TupleObject configC) throws BaseException {
		if (this != ProducerTemplateEnum.NONE) {
			TupleObject _tmp = YAMLUtils.fromYAML("org/pizazz/kafka/config/" + name().toLowerCase() + ".yml");
			TupleObjectHelper.merge(clientC, TupleObjectHelper.getTupleObject(_tmp, KafkaConstant.KEY_CLIENT));
			TupleObjectHelper.merge(configC, TupleObjectHelper.getTupleObject(_tmp, KafkaConstant.KEY_CONFIG));
		}
	}

	public static ProducerTemplateEnum from(String template) {
		if (!StringUtils.isTrimEmpty(template)) {
			template = template.trim().toUpperCase();

			for (ProducerTemplateEnum _item : values()) {
				if (_item.name().equals(template)) {
					return _item;
				}
			}
		}
		return ProducerTemplateEnum.NONE;
	}
}
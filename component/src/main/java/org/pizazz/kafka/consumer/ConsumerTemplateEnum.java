package org.pizazz.kafka.consumer;

import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.common.YAMLUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.kafka.KafkaConstant;

public enum ConsumerTemplateEnum {
	/** 高效 */
	CONSUMER_EFFICIENCY,
	/** 标准 */
	CONSUMER_NORMAL,
	/** 可靠 */
	CONSUMER_RELIABILITY,
	/** 无 */
	NONE;

	public void fill(TupleObject clientC, TupleObject configC) throws AssertException, UtilityException {
		if (this != ConsumerTemplateEnum.NONE) {
			TupleObject _tmp = YAMLUtils.fromYAML(name().toLowerCase() + ".yml");
			clientC.putAll(
					TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(_tmp, KafkaConstant.KEY_CLIENT), clientC));
			configC.putAll(
					TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(_tmp, KafkaConstant.KEY_CONFIG), configC));
		}
	}

	public static ConsumerTemplateEnum from(String template) {
		if (!StringUtils.isTrimEmpty(template)) {
			template = template.trim().toUpperCase();

			for (ConsumerTemplateEnum _item : values()) {
				if (_item.name().equals(template)) {
					return _item;
				}
			}
		}
		return ConsumerTemplateEnum.NONE;
	}
}

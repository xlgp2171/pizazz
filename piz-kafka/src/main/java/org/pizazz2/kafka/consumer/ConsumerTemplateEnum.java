package org.pizazz2.kafka.consumer;

import org.pizazz2.common.StringUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;

/**
 * 消费模板枚举
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public enum ConsumerTemplateEnum {
    /**
     * 高效
     */
    CONSUMER_EFFICIENCY,
    /**
     * 标准
     */
    CONSUMER_NORMAL,
    /**
     * 可靠
     */
    CONSUMER_RELIABILITY,
    /**
     * 无
     */
    NONE;

    public void fill(TupleObject clientC, TupleObject configC) throws ValidateException, UtilityException {
        if (this != ConsumerTemplateEnum.NONE) {
            TupleObject tmp = YAMLUtils.fromYAML(name().toLowerCase() + ".yml");
            clientC.putAll(TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(tmp, KafkaConstant.KEY_CLIENT), clientC));
            configC.putAll(TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(tmp, KafkaConstant.KEY_CONFIG), configC));
        }
    }

    public static ConsumerTemplateEnum from(String template) {
        if (!StringUtils.isTrimEmpty(template)) {
            template = template.trim().toUpperCase();

            for (ConsumerTemplateEnum item : values()) {
                if (item.name().equals(template)) {
                    return item;
                }
            }
        }
        return ConsumerTemplateEnum.NONE;
    }
}

package org.pizazz2.kafka.producer;

import org.pizazz2.common.StringUtils;
import org.pizazz2.common.YAMLUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;

/**
 * 发布组件模板枚举
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public enum ProducerTemplateEnum {
    /**
     * 高效
     */
    PRODUCER_EFFICIENCY,
    /**
     * 标准
     */
    PRODUCER_NORMAL,
    /**
     * 事务
     */
    PRODUCER_TRANSACTION,
    /**
     * 无
     */
    NONE;

    public void fill(TupleObject clientC, TupleObject configC) throws ValidateException, UtilityException {
        if (this != ProducerTemplateEnum.NONE) {
            TupleObject tmp = YAMLUtils.fromYAML(name().toLowerCase() + ".yml");
            clientC.putAll(TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(tmp, KafkaConstant.KEY_CLIENT), clientC));
            configC.putAll(TupleObjectHelper.merge(TupleObjectHelper.getTupleObject(tmp, KafkaConstant.KEY_CONFIG), configC));
        }
    }

    public static ProducerTemplateEnum from(String template) {
        if (!StringUtils.isTrimEmpty(template)) {
            template = template.trim().toUpperCase();

            for (ProducerTemplateEnum item : values()) {
                if (item.name().equals(template)) {
                    return item;
                }
            }
        }
        return ProducerTemplateEnum.NONE;
    }
}

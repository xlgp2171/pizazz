package org.pizazz2.kafka.support;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.ICloseable;
import org.pizazz2.PizContext;
import org.pizazz2.common.*;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz2.kafka.consumer.ConsumerModeEnum;
import org.pizazz2.kafka.consumer.ConsumerTemplateEnum;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.kafka.producer.ProducerModeEnum;
import org.pizazz2.kafka.producer.ProducerTemplateEnum;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 配置转换工具
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class ConfigConvertor implements ICloseable {
	private final TupleObject config = TupleObjectHelper.newObject(2);
	private String template;

	public ConfigConvertor(TupleObject config) throws ValidateException, UtilityException  {
		ValidateUtils.notEmpty("ConfigConvertor", config);
		parse(config);
	}

	private void parse(TupleObject config) throws ValidateException, UtilityException {
		TupleObject clientC = TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CLIENT);
		TupleObject configC = TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CONFIG);
		template = TupleObjectHelper.getString(config, KafkaConstant.KEY_TEMPLATE, StringUtils.EMPTY);
		tryUseTemplate(clientC, configC);
		this.config.append(KafkaConstant.KEY_CLIENT, clientC).append(KafkaConstant.KEY_CONFIG, configC);
	}

	private void tryUseTemplate(TupleObject clientC, TupleObject configC) throws ValidateException, UtilityException {
		if (!StringUtils.isEmpty(template)) {
			ProducerTemplateEnum.from(template).fill(clientC, configC);
			ConsumerTemplateEnum.from(template).fill(clientC, configC);
		}
	}

	public String getTemplateName() {
		return template;
	}

	public String configFromKeys(String... keys) throws ValidateException  {
		ValidateUtils.notEmpty("configFromKeys", keys);
		return TupleObjectHelper.getNestedString(config, null, keys);
	}

	public TupleObject offsetProcessorConfig() {
		return TupleObjectHelper.getNestedTupleObject(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_OFFSET_PROCESSOR);
	}

	public TupleObject transactionProcessorConfig() {
		return TupleObjectHelper.getNestedTupleObject(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_TRANSACTION_PROCESSOR);
	}

	public TupleObject dataProcessorConfig() {
		return TupleObjectHelper.getNestedTupleObject(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_DATA_PROCESSOR);
	}

	public TupleObject senderProcessorConfig() {
		return TupleObjectHelper.getNestedTupleObject(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_SENDER_PROCESSOR);
	}

	public Duration durationValue() {
		int duration = TupleObjectHelper.getNestedInt(config, KafkaConstant.DEF_DURATION, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_DURATION);
		return Duration.ofMillis((duration <= 0 || duration > KafkaConstant.DEF_DURATION_MAX)
				? KafkaConstant.DEF_DURATION : duration);
	}

	public ConsumerModeEnum consumerModeValue() throws ValidateException, KafkaException  {
		String value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_MODE);
		return ConsumerModeEnum.from(value);
	}

	public ProducerModeEnum producerModeValue() throws ValidateException, KafkaException {
		String value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_MODE);
		return ProducerModeEnum.from(value);
	}

	public ConsumerIgnoreEnum consumerIgnoreValue() {
		String value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_IGNORE);

		if (!StringUtils.isEmpty(value)) {
			try {
				return ConsumerIgnoreEnum.from(value);
			} catch (ValidateException | KafkaException e) {
				// do nothing
			}
		}
		return ConsumerIgnoreEnum.NONE;
	}

	public Map<String, Object> kafkaConfig() {
		return TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CLIENT).asMap();
	}

	public String getConsumerGroupId() {
		return TupleObjectHelper.getNestedString(config, PizContext.NAMING, KafkaConstant.KEY_CLIENT,
				ConsumerConfig.GROUP_ID_CONFIG);
	}

	public List<TopicPartition> assignConfig() throws KafkaException {
		List<Object> tmp = TupleObjectHelper.getNestedList(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_TOPIC_PARTITION);

		if (CollectionUtils.isEmpty(tmp)) {
			throw new KafkaException(CodeEnum.KFK_0002, "config 'topicPartition' null");
		}
		List<TopicPartition> tp = new LinkedList<>();

		for (Object item : tmp) {
			String[] partitions = StringUtils.of(item).split(KafkaConstant.SEPARATOR);

			if (partitions.length < 2) {
				throw new KafkaException(CodeEnum.KFK_0003, "topic partition format:T#[NUM]#[NUM]");
			}
			for (int i = 1; i < partitions.length; i++) {
				int partition = NumberUtils.toInt(partitions[i], -1);

				if (partition < 0) {
					throw new KafkaException(CodeEnum.KFK_0003, "partition format:[NUM]");
				}
				tp.add(new TopicPartition(partitions[0], partition));
			}
		}
		return tp;
	}

	public Pattern topicPatternConfig() throws KafkaException {
		String regex = TupleObjectHelper.getNestedString(config, KafkaConstant.KEY_CONFIG, null,
				KafkaConstant.KEY_TOPIC_PATTERN);

		if (StringUtils.isTrimEmpty(regex)) {
			throw new KafkaException(CodeEnum.KFK_0004, "config 'topicPattern' null");
		}
		return Pattern.compile(regex);
	}

	public List<String> topicConfig() throws KafkaException {
		List<Object> tmp = TupleObjectHelper.getNestedList(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_TOPIC);

		if (CollectionUtils.isEmpty(tmp)) {
			throw new KafkaException(CodeEnum.KFK_0005, "config 'topic' null");
		}
		return CollectionUtils.convert(tmp);
	}

	@Override
	public void destroy(Duration timeout) {
		config.clear();
	}
}

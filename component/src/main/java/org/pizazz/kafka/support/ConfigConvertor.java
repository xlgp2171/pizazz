package org.pizazz.kafka.support;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.NumberUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.UtilityException;
import org.pizazz.kafka.KafkaConstant;
import org.pizazz.kafka.consumer.ConsumerIgnoreEnum;
import org.pizazz.kafka.consumer.ConsumerModeEnum;
import org.pizazz.kafka.consumer.ConsumerTemplateEnum;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaError;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.kafka.producer.ProducerModeEnum;
import org.pizazz.kafka.producer.ProducerTemplateEnum;
import org.pizazz.message.ErrorCodeEnum;

public class ConfigConvertor implements ICloseable {
	private final TupleObject config = TupleObjectHelper.newObject(2);
	private String template;

	public ConfigConvertor(TupleObject config) throws AssertException, UtilityException  {
		AssertUtils.assertNotNull("ConfigConvertor", config);
		parse(config);
	}

	private void parse(TupleObject config) throws AssertException, UtilityException {
		if (config.isEmpty()) {
			throw new KafkaError(ErrorCodeEnum.ERR_0005, "base config null");
		}
		TupleObject _clientC = TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CLIENT);
		TupleObject _configC = TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CONFIG);
		template = TupleObjectHelper.getString(config, KafkaConstant.KEY_TEMPLATE, StringUtils.EMPTY);
		tryUseTemplate(_clientC, _configC);
		this.config.append(KafkaConstant.KEY_CLIENT, _clientC).append(KafkaConstant.KEY_CONFIG, _configC);
	}

	private void tryUseTemplate(TupleObject clientC, TupleObject configC) throws AssertException, UtilityException {
		if (!StringUtils.isEmpty(template)) {
			ProducerTemplateEnum.from(template).fill(clientC, configC);
			ConsumerTemplateEnum.from(template).fill(clientC, configC);
		}
	}

	public String configFromKeys(String... keys) throws AssertException  {
		AssertUtils.assertNotNull("configFromKeys", keys, 0);
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
		int _duration = TupleObjectHelper.getNestedInt(config, KafkaConstant.DEF_DURATION, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_DURATION);
		return Duration.ofMillis((_duration <= 0 || _duration > KafkaConstant.DEF_DURATION_MAX)
				? KafkaConstant.DEF_DURATION : _duration);
	}

	public ConsumerModeEnum consumerModeValue() throws AssertException, KafkaException  {
		String _value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_MODE);
		return ConsumerModeEnum.from(_value);
	}

	public ProducerModeEnum producerModeValue() throws AssertException, KafkaException {
		String _value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_MODE);
		return ProducerModeEnum.from(_value);
	}

	public ConsumerIgnoreEnum consumerIgnoreValue() {
		String _value = TupleObjectHelper.getNestedString(config, StringUtils.EMPTY, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_IGNORE);

		if (!StringUtils.isEmpty(_value)) {
			try {
				return ConsumerIgnoreEnum.from(_value);
			} catch (AssertException | KafkaException e) {
			}
		}
		return ConsumerIgnoreEnum.NODE;
	}

	public Map<String, Object> kafkaConfig() {
		return TupleObjectHelper.getTupleObject(config, KafkaConstant.KEY_CLIENT).asMap();
	}

	public String getConsumerGroupId() {
		return TupleObjectHelper.getNestedString(config, Constant.NAMING, KafkaConstant.KEY_CLIENT,
				ConsumerConfig.GROUP_ID_CONFIG);
	}

	public List<TopicPartition> assignConfig() throws KafkaException {
		List<Object> _config = TupleObjectHelper.getNestedList(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_TOPIC_PARTITION);

		if (CollectionUtils.isEmpty(_config)) {
			throw new KafkaException(CodeEnum.KFK_0002, "config 'topicPartition' null");
		}
		List<TopicPartition> _tp = new LinkedList<TopicPartition>();

		for (Object _item : _config) {
			String[] _partitions = StringUtils.of(_item).split(KafkaConstant.SEPARATOR);

			if (_partitions.length < 2) {
				throw new KafkaException(CodeEnum.KFK_0003, "topic partition format:T#[NUM]#[NUM]");
			}
			for (int _i = 1; _i < _partitions.length; _i++) {
				int _partition = NumberUtils.toInt(_partitions[_i], -1);

				if (_partition < 0) {
					throw new KafkaException(CodeEnum.KFK_0003, "partition format:[NUM]");
				}
				_tp.add(new TopicPartition(_partitions[0], _partition));
			}
		}
		return _tp;
	}

	public Pattern topicPatternConfig() throws KafkaException {
		String _regex = TupleObjectHelper.getNestedString(config, KafkaConstant.KEY_CONFIG, null,
				KafkaConstant.KEY_TOPIC_PATTERN);

		if (StringUtils.isTrimEmpty(_regex)) {
			throw new KafkaException(CodeEnum.KFK_0004, "config 'topicPattern' null");
		}
		return Pattern.compile(_regex);
	}

	public List<String> topicConfig() throws KafkaException {
		List<Object> _config = TupleObjectHelper.getNestedList(config, KafkaConstant.KEY_CONFIG,
				KafkaConstant.KEY_TOPIC);

		if (CollectionUtils.isEmpty(_config)) {
			throw new KafkaException(CodeEnum.KFK_0005, "config 'topic' null");
		}
		return CollectionUtils.convert(_config);
	}

	@Override
	public void destroy(Duration timeout) {
		config.clear();
	}
}

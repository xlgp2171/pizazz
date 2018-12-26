package org.pizazz.kafka.producer;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.Constant;
import org.pizazz.common.StringUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionProcessor implements ITransactionProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProcessor.class);
	private ProducerModeEnum mode;

	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	@Override
	public <K, V> void initTransactions(KafkaProducer<K, V> producer) throws KafkaException {
		if (mode.isTransaction()) {
			try {
				producer.initTransactions();
				LOGGER.info("producer init transactions");
			} catch (Exception e) {
				throw new KafkaException(CodeEnum.KFK_0013, "about transaction:" + e.getMessage(), e);
			}
		}
	}

	@Override
	public <K, V> void beginTransaction(KafkaProducer<K, V> producer) throws KafkaException {
		if (mode.isTransaction()) {
			try {
				producer.beginTransaction();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("producer begin transactions");
				}
			} catch (Exception e) {
				throw new KafkaException(CodeEnum.KFK_0013, "about transaction:" + e.getMessage(), e);
			}
		}
	}

	@Override
	public <K, V> void commitTransaction(KafkaProducer<K, V> producer, Map<TopicPartition, OffsetAndMetadata> offsets,
			String groupId) throws KafkaException {
		if (mode.isTransaction()) {
			try {
				if (offsets == null || StringUtils.isBlank(groupId)) {
					producer.sendOffsetsToTransaction(offsets, groupId);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.info("producer send offset to transactions:" + offsets + ",groupId=" + groupId);
					}
				}
				producer.commitTransaction();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.info("producer commit transactions");
				}
			} catch (Exception e) {
				throw new KafkaException(CodeEnum.KFK_0013, "commit transaction:" + e.getMessage(), e);
			}
		}
	}

	@Override
	public <K, V> void abortTransaction(KafkaProducer<K, V> producer) throws KafkaException {
		if (mode.isTransaction()) {
			try {
				producer.abortTransaction();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.info("producer about transactions");
				}
			} catch (Exception e) {
				throw new KafkaException(CodeEnum.KFK_0013, "about transaction:" + e.getMessage(), e);
			}
		}
	}

	@Override
	public void set(ProducerModeEnum mode) {
		this.mode = mode;
	}

	@Override
	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config) {
		boolean _transactionMode = mode == null ? false : mode.isTransaction();

		if (_transactionMode && !config.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG)) {
			config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, Constant.NAMING);
			LOGGER.info(new StringBuilder("set production config:").append(ProducerConfig.TRANSACTIONAL_ID_CONFIG)
					.append("=").append(Constant.NAMING).append(",mode=").append(mode).toString());
		}
		return config;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
	}
}

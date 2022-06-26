package org.pizazz2.kafka.producer;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.PizContext;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务处理组件
 * FIXME 实现需要参考应用场景
 *
 * @author xlgp2171
 * @version 2.1.220625
 */
public class TransactionProcessor implements ITransactionProcessor {
    private final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);
    private ProducerModeEnum mode;

    @Override
    public void initialize(TupleObject config) throws KafkaException {
    }

    @Override
    public <K, V> void initTransactions(KafkaProducer<K, V> producer) {
        if (mode.isTransaction()) {
            producer.initTransactions();

            if (KafkaConstant.DEBUG_MODE) {
                logger.debug(KafkaConstant.LOG_TAG + "producer init transactions");
            }
        }
    }

    @Override
    public <K, V> void beginTransaction(KafkaProducer<K, V> producer) throws KafkaException {
        if (mode.isTransaction()) {
            try {
                producer.beginTransaction();

                if (KafkaConstant.DEBUG_MODE) {
                    logger.debug(KafkaConstant.LOG_TAG + "producer begin transactions");
                }
            } catch (RuntimeException e) {
                throw new KafkaException(CodeEnum.KFK_0013, "begin transaction:" + e.getMessage(), e);
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

                    if (KafkaConstant.DEBUG_MODE) {
                        logger.debug(KafkaConstant.LOG_TAG + "producer send offset to transactions:" + offsets +
                                ",groupId=" + groupId);
                    }
                }
                producer.commitTransaction();

                if (KafkaConstant.DEBUG_MODE) {
                    logger.debug(KafkaConstant.LOG_TAG + "producer commit transactions");
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

                if (KafkaConstant.DEBUG_MODE) {
                    logger.debug(KafkaConstant.LOG_TAG + "producer about transactions");
                }
            } catch (RuntimeException e) {
                throw new KafkaException(CodeEnum.KFK_0013, "about transaction:" + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setMode(ProducerModeEnum mode) {
        this.mode = mode;
    }

    @Override
    public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config) {
        boolean transactionMode = mode != null && mode.isTransaction();

        if (transactionMode && !config.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG)) {
            config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, PizContext.NAMING);
            logger.info(KafkaConstant.LOG_TAG + "set production config:" + ProducerConfig.TRANSACTIONAL_ID_CONFIG +
                    "=" + PizContext.NAMING + ",mode=" + mode);
        }
        return config;
    }

    @Override
    public void destroy(Duration timeout) {
    }
}

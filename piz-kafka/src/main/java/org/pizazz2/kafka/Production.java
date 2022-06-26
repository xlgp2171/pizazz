package org.pizazz2.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.*;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.kafka.producer.ITransactionProcessor;
import org.pizazz2.kafka.producer.SenderProcessor;
import org.pizazz2.kafka.producer.TransactionProcessor;
import org.pizazz2.kafka.core.AbstractClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka消息发布组件
 * TODO 未包含事务处理API
 *
 * @param <K> 消息Key
 * @param <V> 消息Value
 *
 * @author xlgp2171
 * @version 2.1.220625
 */
public class Production<K, V> extends AbstractClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Production.class);

    private KafkaProducer<K, V> producer;
    protected ITransactionProcessor transaction;
    protected SenderProcessor<K, V> processor;

    public Production(TupleObject configure) throws BaseRuntimeException, BaseException {
        super(configure);
    }

    @Override
    protected void initialize() throws BaseRuntimeException, BaseException {
        // 获取发布事务配置
        updateConfig(super.getConvertor().transactionProcessorConfig());
        // 构建事务处理器
        transaction = super.loadPlugin("classpath", ITransactionProcessor.class, null, true);
        transaction.setMode(getConvertor().producerModeValue());
        // 构建发送消息处理组件
        processor = new SenderProcessor<>(getConvertor().producerModeValue(), getConvertor().senderProcessorConfig());
        // 构建kafka发送实例
        producer = new KafkaProducer<>(transaction.optimizeKafkaConfig(getConvertor().kafkaConfig()));
        // 尝试初始化事务
        transaction.initTransactions(producer);
        LOGGER.info(KafkaConstant.LOG_TAG + "production initialized,config=" + super.getConfig());
    }

//    public Production<K, V> beginTransaction() throws KafkaException {
//        try {
//            transaction.beginTransaction(producer);
//        } catch (KafkaException e) {
//            LOGGER.error(e.getMessage(), e);
//            throw e;
//        }
//        return this;
//    }
//
//    public Production<K, V> commitTransaction() throws KafkaException {
//        return commitTransaction(null, null);
//    }
//
//    public Production<K, V> commitTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String groupId)
//            throws KafkaException {
//        try {
//            transaction.commitTransaction(producer, offsets, groupId);
//        } catch (KafkaException e) {
//            LOGGER.error(e.getMessage(), e);
//            throw e;
//        }
//        return this;
//    }
//
//    public void abortTransaction() throws KafkaException {
//        try {
//            transaction.abortTransaction(producer);
//        } catch (KafkaException e) {
//            LOGGER.error(e.getMessage(), e);
//            throw e;
//        }
//    }

    public void sent(String topic, V value) throws KafkaException {
        sent(new ProducerRecord<>(topic, value), null);
    }

    public void sent(ProducerRecord<K, V> record) throws KafkaException {
        sent(record, null);
    }

    public Future<RecordMetadata> sent(ProducerRecord<K, V> record, Callback callback) throws KafkaException {
        return processor.sentData(producer, record, callback);
    }

    public void flush() {
        producer.flush();
    }

    public KafkaProducer<K, V> getTarget() {
        return producer;
    }

    @Override
    public void destroy(Duration timeout) {
        if (producer != null) {
            flush();
            super.destroy(timeout);
            unloadPlugin(transaction, timeout);
            producer.close(timeout);
            LOGGER.info(KafkaConstant.LOG_TAG + "production destroyed,timeout=" + timeout);
        }
    }
}

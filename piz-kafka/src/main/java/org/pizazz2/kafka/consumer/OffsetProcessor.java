package org.pizazz2.kafka.consumer;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.PizContext;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.ResourceUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 偏移量处理工具
 *
 * @author xlgp2171
 * @version 2.1.211201
 */
public class OffsetProcessor implements IOffsetProcessor {
    private final Logger logger = LoggerFactory.getLogger(OffsetProcessor.class);
    /** 已经提交的offset */
    protected final Map<TopicPartition, OffsetAndMetadata> offsetCommitted;
    /** 当前缓存的offset */
    protected final Map<TopicPartition, OffsetAndMetadata> offsetCache;
    private ConsumerModeEnum mode;
    private ConsumerIgnoreEnum ignore;
    private int retries;

    private final OffsetCommitCallback callback = (offsets, e) -> {
        if (e != null) {
            logger.error(KafkaConstant.LOG_TAG + "consumer commit:" + offsets, e);
        } else if (offsets != null) {
            markCommitted(offsets);
        }
    };

    public OffsetProcessor() {
        offsetCommitted = new HashMap<>();
        offsetCache = new HashMap<>();
    }

    @Override
    public void initialize(TupleObject config) throws KafkaException {
        retries = TupleObjectHelper.getInt(config, "retries", 1);
    }

    private <K, V> void offsetCommit(KafkaConsumer<K, V> consumer, boolean force, int retries) throws KafkaException {
        Map<TopicPartition, OffsetAndMetadata> tmp = getOffsetCache();
        // 若同步或者强制提交
        if (mode.isSync() || force) {
            try {
                // 非强制情况下同步每一个
                if (mode.isEach() && !force) {
                    if (!CollectionUtils.isEmpty(tmp)) {
                        consumer.commitSync(tmp);
                    }
                } else {
                    consumer.commitSync();
                }
            } catch (Exception e) {
                // 是否重试
                if (retries >= this.retries) {
                    if (ignore.offsetThrowable()) {
                        throw new KafkaException(CodeEnum.KFK_0006, "consumer commit:" + tmp, e);
                    } else {
                        logger.warn(KafkaConstant.LOG_TAG + "consumer commit:" + tmp, e);
                    }
                } else {
                    // 同步情况下提交重试
                    offsetCommit(consumer, force, retries + 1);
                }
                return;
            }
            markCommitted(tmp);
        } else if (mode.isEach()) {
            // 异步情况下每一个
            consumer.commitAsync(tmp, callback);
        } else {
            consumer.commitAsync(callback);
        }
        if (KafkaConstant.DEBUG_MODE) {
            logger.debug(KafkaConstant.LOG_TAG + "consumer commit:" + tmp);
        }
    }

    private void markCommitted(Map<TopicPartition, OffsetAndMetadata> offsets) {
        // 缓存已提交的信息
        synchronized (offsetCommitted) {
            offsets.forEach((k, v) -> {
                if (offsetCommitted.containsKey(k)) {
                    if (v.offset() > offsetCommitted.get(k).offset()) {
                        offsetCommitted.put(k, v);
                    }
                } else {
                    offsetCommitted.put(k, v);
                }
            });
        }
        if (KafkaConstant.DEBUG_MODE) {
            logger.debug(KafkaConstant.LOG_TAG + "consumer mark committed:" + offsets);
        }
    }
    @Override
    public <K, V> void batch(KafkaConsumer<K, V> consumer, Collection<ConsumerRecord<K, V>> records)
            throws KafkaException {
        for (ConsumerRecord<K, V> item : records) {
            each(consumer, item);
        }
    }

    @Override
    public <K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record) throws KafkaException {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        // 若已有T&P提交，则验证offset大小
        if (offsetCommitted.containsKey(tp)) {
            // 若当前提交是否大于已提交offset，则写入缓存
            if (record.offset() > offsetCommitted.get(tp).offset()) {
                setAndCommit(consumer, record, tp);
            } else if (KafkaConstant.DEBUG_MODE) {
                logger.debug(KafkaConstant.LOG_TAG + tp + " consumed:" + record.offset());
            }
        } else {
            setAndCommit(consumer, record, tp);
        }
    }

    private <K, V> void setAndCommit(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record, TopicPartition tp)
            throws KafkaException {
        synchronized (offsetCache) {
            offsetCache.put(tp, new OffsetAndMetadata(record.offset()));
        }
        if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && mode.isEach()) {
            offsetCommit(consumer, false, 0);
        }
    }

    @Override
    public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) throws KafkaException {
        // 若手动提交且一轮提交且无异常
        if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && e == null) {
            offsetCommit(consumer, false, 0);

            if (KafkaConstant.DEBUG_MODE) {
                logger.debug(KafkaConstant.LOG_TAG + "consumer commit sync:" + mode.isSync());
            }
        }
    }

    @Override
    public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config) {
        boolean commitMode = mode == null || mode.isAuto();
        // 若Kafka设置同ConsumerModeEnum设置不相同
        boolean tmp = !config.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) ||
				(mode != null && commitMode != ResourceUtils.getBoolean(
				        config, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true));

        if (tmp) {
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, commitMode);
            logger.info(KafkaConstant.LOG_TAG + "set subscription config:" + ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG +
                    "=" + commitMode + ",mode=" + mode);
        }
        if (!config.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            config.put(ConsumerConfig.GROUP_ID_CONFIG, PizContext.NAMING);
            logger.info(KafkaConstant.LOG_TAG + "set subscription config:" + ConsumerConfig.GROUP_ID_CONFIG + "=" +
                    PizContext.NAMING);
        }
        return config;
    }

    @Override
    public <K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer, final ConsumerRebalanceListener listener) {
        return new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // 若非自动提交且统一提交
                if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && !mode.isEach()) {
                    try {
                        offsetCommit(consumer, true, 0);
                    } catch (KafkaException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (listener != null) {
                    listener.onPartitionsRevoked(partitions);
                }
                logger.info(KafkaConstant.LOG_TAG + "subscription partitions revoked:" +
                        CollectionUtils.toString(partitions));
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                restOffsetCommitted();

                synchronized (offsetCache) {
                    offsetCache.clear();
                }
                if (listener != null) {
                    listener.onPartitionsAssigned(partitions);
                }
                logger.info(KafkaConstant.LOG_TAG + "subscription partitions assigned:" +
                        CollectionUtils.toString(partitions));
            }
        };
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> getOffsetCache() {
        synchronized (offsetCache) {
            return new HashMap<>(offsetCache);
        }
    }

    @Override
    public void restOffsetCommitted() {
        synchronized (offsetCommitted) {
            offsetCommitted.clear();
        }
        logger.info(KafkaConstant.LOG_TAG + "consumer committed reset");
    }

    @Override
    public void set(ConsumerModeEnum mode, ConsumerIgnoreEnum ignore) {
        this.mode = mode;
        this.ignore = ignore;
    }

    protected ConsumerModeEnum getMode() {
        return mode;
    }

    protected ConsumerIgnoreEnum getIgnore() {
        return ignore;
    }

    @Override
    public void destroy(Duration timeout) {
        restOffsetCommitted();
        offsetCache.clear();
        logger.info(KafkaConstant.LOG_TAG + "subscription offset processor destroyed, timeout=" + timeout);
    }
}

package org.pizazz2.kafka.consumer;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.IObject;
import org.pizazz2.PizContext;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.ResourceUtils;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 偏移量处理工具
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class OffsetProcessor implements IOffsetProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffsetProcessor.class);
    protected final Map<TopicPartition, OffsetAndMetadata> offsetCommitted;
    protected final Map<TopicPartition, OffsetAndMetadata> offsetCache;
    private ConsumerModeEnum mode;
    private ConsumerIgnoreEnum ignore;

    private final OffsetCommitCallback callback = (offsets, e) -> {
        if (e != null) {
            LOGGER.error("consumer commit:" + offsets, e);
        } else if (offsets != null) {
            markCommitted(offsets);
        }
    };

    public OffsetProcessor() {
        offsetCommitted = new HashMap<>();
        offsetCache = new HashMap<>();
    }

    @Override
    public void initialize(IObject config) throws KafkaException {
    }

    private <K, V> void offsetCommit(KafkaConsumer<K, V> consumer, boolean force) throws KafkaException {
        Map<TopicPartition, OffsetAndMetadata> tmp = getOffsetCache();

        if (mode.isSync() || force) {
            try {
                if (mode.isEach() && !force) {
                    if (!CollectionUtils.isEmpty(tmp)) {
                        consumer.commitSync(tmp);
                    }
                } else {
                    consumer.commitSync();
                }
            } catch (Exception e) {
                if (ignore.offsetThrowable()) {
                    throw new KafkaException(CodeEnum.KFK_0006, "consumer commit:" + tmp, e);
                } else {
                    LOGGER.warn("consumer commit:" + tmp, e);
                    return;
                }
            }
            markCommitted(tmp);
        } else if (mode.isEach()) {
            consumer.commitAsync(tmp, callback);
        } else {
            consumer.commitAsync(callback);
        }
        if (KafkaConstant.DEBUG_MODE) {
            LOGGER.debug("consumer commit:" + tmp);
        }
    }

    private void markCommitted(Map<TopicPartition, OffsetAndMetadata> offsets) {
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
            LOGGER.debug("consumer mark committed:" + offsets);
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
                LOGGER.debug(tp + " consumed:" + record.offset());
            }
        } else {
            setAndCommit(consumer, record, tp);
        }
    }

    private <K, V> void setAndCommit(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record, TopicPartition tp) throws KafkaException {
        synchronized (offsetCache) {
            offsetCache.put(tp, new OffsetAndMetadata(record.offset()));
        }
        if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && mode.isEach()) {
            offsetCommit(consumer, false);
        }
    }

    @Override
    public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) throws KafkaException {
        // 若手动提交且一轮提交且无异常
        if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && e == null) {
            offsetCommit(consumer, false);

            if (KafkaConstant.DEBUG_MODE) {
                LOGGER.debug("consumer commit sync:" + mode.isSync());
            }
        }
    }

    @Override
    public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config) {
        boolean commitMode = mode == null || mode.isAuto();
        // 若Kafka设置同ConsumerModeEnum设置不相同
        boolean tmp = !config.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) ||
				(mode != null && commitMode != ResourceUtils.getBoolean(config, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true));

        if (tmp) {
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, commitMode);
            LOGGER.info("set subscription config:" + ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=" + commitMode + ",mode=" + mode);
        }
        if (!config.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            config.put(ConsumerConfig.GROUP_ID_CONFIG, PizContext.NAMING);
            LOGGER.info("set subscription config:" + ConsumerConfig.GROUP_ID_CONFIG + "=" + PizContext.NAMING);
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
                        offsetCommit(consumer, true);
                    } catch (KafkaException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                if (listener != null) {
                    listener.onPartitionsRevoked(partitions);
                }
                LOGGER.info("subscription partitions revoked:" + CollectionUtils.toString(partitions));
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
                LOGGER.info("subscription partitions assigned:" + CollectionUtils.toString(partitions));
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
        LOGGER.info("consumer committed reset");
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
        LOGGER.info("subscription offset processor destroyed, timeout=" + timeout);
    }
}

package org.pizazz.kafka.consumer;

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
import org.pizazz.Constant;
import org.pizazz.common.BooleanUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OffsetProcessor implements IOffsetProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(OffsetProcessor.class);
	protected final Map<TopicPartition, OffsetAndMetadata> offsetCommitted;
	protected final Map<TopicPartition, OffsetAndMetadata> offsetCache;
	private ConsumerModeEnum mode;
	private ConsumerIgnoreEnum ignore;

	private final OffsetCommitCallback callback = new OffsetCommitCallback() {
		@Override
		public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
			if (e != null) {
				LOGGER.error("consumer commit:" + offsets, e);
			} else if (offsets != null) {
				markCommitted(offsets);
			}
		}
	};

	public OffsetProcessor() {
		offsetCommitted = new HashMap<TopicPartition, OffsetAndMetadata>();
		offsetCache = new HashMap<TopicPartition, OffsetAndMetadata>();
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	private <K, V> void offsetCommit(KafkaConsumer<K, V> consumer, boolean force) throws KafkaException {
		Map<TopicPartition, OffsetAndMetadata> _tmp = getOffsetCache();

		if (mode.isSync() || force) {
			try {
				if (mode.isEach() && !force) {
					consumer.commitSync(_tmp);
				} else {
					consumer.commitSync();
				}
			} catch (Exception e) {
				if (ignore.offsetThrowable()) {
					throw new KafkaException(CodeEnum.KFK_0006, "consumer commit:" + _tmp, e);
				} else {
					LOGGER.warn("consumer commit:" + _tmp, e);
					return;
				}
			}
			markCommitted(_tmp);
		} else if (mode.isEach()){
			consumer.commitAsync(_tmp, callback);
		} else {
			consumer.commitAsync(callback);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consumer commit:" + _tmp);
		}
	}

	private void markCommitted(Map<TopicPartition, OffsetAndMetadata> offsets) {
		synchronized (offsetCommitted) {
			offsets.forEach((_k, _v) -> {
				if (offsetCommitted.containsKey(_k)) {
					if (_v.offset() > offsetCommitted.get(_k).offset()) {
						offsetCommitted.put(_k, _v);
					}
				} else {
					offsetCommitted.put(_k, _v);
				}
			});
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consumer committid:" + offsets);
		}
	}

	@Override
	public <K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record) throws KafkaException {
		TopicPartition _tp = new TopicPartition(record.topic(), record.partition());
		// 若已有T&P提交，则验证offset大小
		if (offsetCommitted.containsKey(_tp)) {
			// 若当前提交是否大于已提交offset，则写入缓存
			if (record.offset() > offsetCommitted.get(_tp).offset()) {
				synchronized (offsetCache) {
					offsetCache.put(_tp, new OffsetAndMetadata(record.offset()));

					if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto()) {
						offsetCommit(consumer, false);
					}
				}
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(_tp + " consumed:" + record.offset());
			}
		} else {
			synchronized (offsetCache) {
				offsetCache.put(_tp, new OffsetAndMetadata(record.offset()));

				if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto()) {
					offsetCommit(consumer, false);
				}
			}
		}

	}

	@Override
	public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) throws KafkaException {
		// 若手动提交且一轮提交且无异常
		if (mode != ConsumerModeEnum.MANUAL_NONE_NONE && !mode.isAuto() && e == null) {
			offsetCommit(consumer, false);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consumer commit sync:" + mode.isSync());
			}
		}
	}

	@Override
	public Map<String, Object> optimizeKafkaConfig(Map<String, Object> config) {
		boolean _commitMode = mode == null ? true : mode.isAuto();
		// 若Kafka设置同ConsumerModeEnum设置不相同
		if (!config.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
				|| (mode != null && _commitMode != BooleanUtils
						.toBoolean(StringUtils.of(config.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)), true))) {
			config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, _commitMode);
			LOGGER.info(new StringBuilder("set subscription config:").append(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
					.append("=").append(_commitMode).append(",mode=").append(mode).toString());
		}
		if (!config.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
			config.put(ConsumerConfig.GROUP_ID_CONFIG, Constant.NAMING);
			LOGGER.info(new StringBuilder("set subscription config:").append(ConsumerConfig.GROUP_ID_CONFIG).append("=")
					.append(Constant.NAMING).toString());
		}
		return config;
	}

	@Override
	public <K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer,
			final ConsumerRebalanceListener listener) {
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
			return new HashMap<TopicPartition, OffsetAndMetadata>(offsetCache);
		}
	}

	@Override
	public void restOffsetCommitted() {
		synchronized (offsetCommitted) {
			offsetCommitted.clear();
		}
		LOGGER.info("consumer committid reset");
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
	public void destroy(Duration timeout) throws BaseException {
		restOffsetCommitted();
		offsetCache.clear();
		LOGGER.info("subscription offset processor destroyed,timeout=" + timeout);
	}
}

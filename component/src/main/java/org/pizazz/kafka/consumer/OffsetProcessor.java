package org.pizazz.kafka.consumer;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.common.BooleanUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OffsetProcessor implements IOffsetProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(OffsetProcessor.class);
	protected final Map<TopicPartition, OffsetAndMetadata> offsetCommitted;
	protected final ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetCache;
	private ConsumerModeEnum mode;

	private final OffsetCommitCallback callback = new OffsetCommitCallback() {
		@Override
		public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
			if (e != null) {
				LOGGER.error("consumer commit:" + offsets, e);
			} else if (offsets != null) {
				offsetCommitted(offsets);
			}
		}
	};

	public OffsetProcessor() {
		offsetCommitted = new HashMap<TopicPartition, OffsetAndMetadata>();
		offsetCache = new ConcurrentHashMap<TopicPartition, OffsetAndMetadata>();
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
	}

	private <K, V> void offsetCommit(KafkaConsumer<K, V> consumer) {
		Map<TopicPartition, OffsetAndMetadata> _tmp;

		synchronized (offsetCache) {
			_tmp = new HashMap<TopicPartition, OffsetAndMetadata>(offsetCache);
		}
		if (mode.isSync()) {
			try {
				consumer.commitSync(_tmp);
			} catch (Exception e) {
				LOGGER.error("consumer commit:" + _tmp, e);
				return;
			}
			offsetCommitted(_tmp);
		} else {
			consumer.commitAsync(_tmp, callback);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consumer commit:" + _tmp);
		}
	}

	private void offsetCommitted(Map<TopicPartition, OffsetAndMetadata> offsets) {
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
	public <K, V> void each(KafkaConsumer<K, V> consumer, ConsumerRecord<K, V> record) {
		if (!mode.isAuto() && mode.isEach()) {
			TopicPartition _tp = new TopicPartition(record.topic(), record.partition());
			// 若已有T&P提交，则验证offset大小
			if (offsetCommitted.containsKey(_tp)) {
				// 若当前提交是否大于已提交offset，则写入缓存
				if (record.offset() > offsetCommitted.get(_tp).offset()) {
					synchronized (offsetCache) {
						offsetCache.put(_tp, new OffsetAndMetadata(record.offset()));
						offsetCommit(consumer);
					}
				} else if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(_tp + " consumed:" + record.offset());
				}
			} else {
				synchronized (offsetCache) {
					offsetCache.put(_tp, new OffsetAndMetadata(record.offset()));
					offsetCommit(consumer);
				}
			}
		}
	}

	@Override
	public <K, V> void complete(KafkaConsumer<K, V> consumer, KafkaException e) {
		// 若手动提交且一轮提交且无异常
		if (!mode.isAuto() && !mode.isEach() && e == null) {
			offsetCommit(consumer);

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
		return config;
	}

	@Override
	public <K, V> ConsumerRebalanceListener getRebalanceListener(KafkaConsumer<K, V> consumer,
			final ConsumerRebalanceListener listener) {
		return new ConsumerRebalanceListener() {
			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
				// 若非自动提交且统一提交
				if (!mode.isAuto() && !mode.isEach()) {
					offsetCommit(consumer);
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
	public void restOffsetCommitted() {
		synchronized (offsetCommitted) {
			offsetCommitted.clear();
		}
		LOGGER.info("consumer committid reset");
	}

	@Override
	public void setMode(ConsumerModeEnum mode) {
		this.mode = mode;
	}

	protected ConsumerModeEnum getMode() {
		return mode;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		restOffsetCommitted();
		offsetCache.clear();
		LOGGER.info("subscription offset processor destroyed,timeout=" + timeout);
	}
}

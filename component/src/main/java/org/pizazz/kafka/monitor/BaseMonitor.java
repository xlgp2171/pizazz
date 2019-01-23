package org.pizazz.kafka.monitor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.IPlugin;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.KafkaConstant;
import org.pizazz.kafka.Management;
import org.pizazz.tool.PThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMonitor<K, V> implements IPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseMonitor.class);
	protected final Management<K, V> management_ = new Management<K, V>();
	protected final ConcurrentSkipListSet<NodeEntity> nodes_;
	protected final ConcurrentSkipListSet<ConsumerEntity> consumers_;
	private ScheduledExecutorService scheduled;

	public BaseMonitor() {
		nodes_ = new ConcurrentSkipListSet<NodeEntity>((_o1, _o2) -> _o1.compareTo(_o2));
		consumers_ = new ConcurrentSkipListSet<ConsumerEntity>((_o1, _o2) -> _o1.compareTo(_o2));
	}

	@Override
	public void initialize(TupleObject config) throws BaseException {
		management_.initialize(config);
		scheduled = Executors.newScheduledThreadPool(2, new PThreadFactory(KafkaConstant.KEY_KAFKA, true));
		LOGGER.info("BaseMonitor initialized,config=" + config);
	}

	public final void activate(Duration period, boolean realtime) {
		Duration _period = (period == null || period.isZero() || period.isNegative()) ? Duration.ofMillis(30000)
				: period;
		scheduled.scheduleAtFixedRate(() -> resetConsumerEntities(realtime), 1000, _period.toMillis(),
				TimeUnit.MILLISECONDS);
		scheduled.scheduleWithFixedDelay(() -> refreshConsumerEntities(realtime), 1000 * 2, _period.toMillis() / 3,
				TimeUnit.MILLISECONDS);
		LOGGER.info("BaseMonitor scheduled,period=" + period);
	}

	protected Collection<ConsumerGroupListing> getGroups() {
		Collection<ConsumerGroupListing> _groups = null;
		try {
			// 获取所有GroupId
			_groups = management_.getGroups().valid().get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.warn("getGroups:" + e.getMessage(), e);
		}
		return _groups;
	}

	private List<String> getGroupIds() {
		Collection<ConsumerGroupListing> _groups = getGroups();
		String[] _tmp = ArrayUtils.EMPTY_STRING;

		if (!CollectionUtils.isEmpty(_groups)) {
			// 获取所有TopicPartition
			_tmp = _groups.stream().map(_item -> _item.groupId()).toArray(String[]::new);
		}
		return Arrays.asList(_tmp);
	}

	private void refreshConsumerEntities(boolean realtime) {
		ConcurrentSkipListSet<ConsumerEntity> _consumers = new ConcurrentSkipListSet<ConsumerEntity>(
				(_o1, _o2) -> _o1.compareTo(_o2));

		getGroups().stream().forEach(_item -> {
			Map<TopicPartition, OffsetAndMetadata> _tmp = null;
			try {
				_tmp = management_.getTopicPartition(_item.groupId()).get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.warn("listConsumerGroupOffsets:" + e.getMessage(), e);
			}
			if (!CollectionUtils.isEmpty(_tmp)) {
				refreshConsumerEntities(_item, _tmp, realtime ? null : _consumers);
			}
		});
		if (!realtime) {
			if (_consumers.isEmpty()) {
				consumers_.clear();
			} else {
				resetConsumerEntities(_consumers, (_item, _target) -> _item
						.setOffsetAndMetadata(_target.getOffsetAndMetadata()).setEndOffset(_target.getEndOffset()));
			}
		}
	}

	private void refreshConsumerEntities(ConsumerGroupListing group, Map<TopicPartition, OffsetAndMetadata> data,
			ConcurrentSkipListSet<ConsumerEntity> consumers) {
		if (consumers == null) {
			refreshConsumerEntities(group, data);
		} else {
			resetConsumerEntities(group, data, consumers);
		}
	}

	protected void refreshConsumerEntities(ConsumerGroupListing group, Map<TopicPartition, OffsetAndMetadata> data) {
		management_.getEndOffsets(data.keySet()).forEach((_tp, _l) -> {
			ConsumerEntity _source = new ConsumerEntity(group, _tp);
			ConsumerEntity _target = consumers_.ceiling(_source);

			if (_target != null && _source.compareTo(_target) == 0) {
				_target.setOffsetAndMetadata(data.get(_tp)).setEndOffset(_l);
			}
		});
	}

	protected void resetConsumerEntities(ConsumerGroupListing group, Map<TopicPartition, OffsetAndMetadata> data,
			ConcurrentSkipListSet<ConsumerEntity> consumers) {
		management_.getEndOffsets(data.keySet()).forEach(
				(_tp, _l) -> addConsumerEntities(new ConsumerEntity(group, _tp, data.get(_tp), _l), consumers));
	}

	private void addConsumerEntities(ConsumerEntity entity, ConcurrentSkipListSet<ConsumerEntity> consumers) {
		if (!consumers_.contains(entity)) {
			consumers_.add(entity);
		}
		consumers.add(entity);
	}

	private void resetConsumerEntities(boolean realtime) {
		ConcurrentSkipListSet<ConsumerEntity> _consumers = new ConcurrentSkipListSet<ConsumerEntity>(
				(_o1, _o2) -> _o1.compareTo(_o2));

		synchronized (nodes_) {
			nodes_.clear();
			management_.describedGroups(getGroupIds()).values().parallelStream().forEach(_item -> {
				ConsumerGroupDescription _description = null;
				try {
					_description = _item.get();
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.warn("getGroupDescription:" + e.getMessage(), e);
				}
				if (_description != null) {
					ConsumerGroupListing _group = new ConsumerGroupListing(_description.groupId(),
							_description.isSimpleConsumerGroup());
					nodes_.add(new NodeEntity(_description.coordinator(), _group, _description.state()));

					if (realtime) {
						resetConsumerEntities(_description, _group, _consumers);
					}
				}
			});
		}
		if (realtime) {
			if (_consumers.isEmpty()) {
				consumers_.clear();
			} else {
				resetConsumerEntities(_consumers, (_item, _target) -> _item.setHost(_target.getHost()));
			}
		}
	}

	protected void resetConsumerEntities(ConsumerGroupDescription description, ConsumerGroupListing group,
			ConcurrentSkipListSet<ConsumerEntity> consumers) {
		description.members()
				.stream().forEach(_item -> _item.assignment().topicPartitions().stream().forEach(
						_tp -> addConsumerEntities(new ConsumerEntity(_item.consumerId(), _item.host(), group, _tp),
								consumers)));
	}

	private void resetConsumerEntities(ConcurrentSkipListSet<ConsumerEntity> consumers,
			BiConsumer<ConsumerEntity, ConsumerEntity> consumer) {
		consumers_.stream().forEach(_item -> {
			ConsumerEntity _target = consumers.ceiling(_item);

			if (_target != null && _item.compareTo(_target) == 0) {
				consumer.accept(_item, _target);
			} else {
				consumers_.remove(_item);
			}
		});
	}

	public final ConsumerEntity[] consumerCache() {
		return consumers_.stream().map(_item -> _item.clone()).toArray(ConsumerEntity[]::new);
	}

	public final NodeEntity[] nodeCache() {
		synchronized (nodes_) {
			return nodes_.stream().map(_item -> _item.clone()).toArray(NodeEntity[]::new);
		}
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			scheduled.shutdownNow();
		} else {
			scheduled.shutdown();
		}
		SystemUtils.destroy(management_, timeout);
		LOGGER.info("BaseMonitor destroyed,timeout=" + timeout);
	}
}

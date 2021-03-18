package org.pizazz2.kafka.monitor;

import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.ICloseable;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.kafka.KafkaConstant;
import org.pizazz2.kafka.Management;
import org.pizazz2.tool.PizThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * 简单的kafka监控组件
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class SimpleMonitor<K, V> implements ICloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMonitor.class);
    protected final Management<K, V> management;
    protected final ConcurrentSkipListSet<NodeEntity> nodes;
    protected final ConcurrentSkipListSet<ConsumerEntity> consumers;
    private final ScheduledExecutorService scheduled;

    public SimpleMonitor(TupleObject config) throws BaseException {
        management = new Management<>(config);
        nodes = new ConcurrentSkipListSet<>(NodeEntity::compareTo);
        consumers = new ConcurrentSkipListSet<>(ConsumerEntity::compareTo);
        scheduled = new ScheduledThreadPoolExecutor(2, new PizThreadFactory(KafkaConstant.KEY_KAFKA, true));
    }

    public final void activate(Duration period, boolean realtime) {
        Duration tmp = (period == null || period.isZero() || period.isNegative()) ? Duration.ofMillis(30000) : period;
        scheduled.scheduleAtFixedRate(() -> resetConsumerEntities(realtime), 1000, tmp.toMillis(), TimeUnit.MILLISECONDS);
        scheduled.scheduleWithFixedDelay(() -> refreshConsumerEntities(realtime), 1000 * 2, tmp.toMillis() / 3, TimeUnit.MILLISECONDS);
        LOGGER.info("SimpleMonitor scheduled,period=" + period);
    }

    protected Collection<ConsumerGroupListing> getGroups() {
        Collection<ConsumerGroupListing> groups = null;
        try {
            // 获取所有GroupId
            groups = management.getGroups().valid().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("getGroups:" + e.getMessage(), e);
        }
        return groups;
    }

    private List<String> getGroupIds() {
        Collection<ConsumerGroupListing> groups = getGroups();
        String[] tmp = ArrayUtils.EMPTY_STRING;

        if (!CollectionUtils.isEmpty(groups)) {
            // 获取所有TopicPartition
            tmp = groups.stream().map(ConsumerGroupListing::groupId).toArray(String[]::new);
        }
        return Arrays.asList(tmp);
    }

    private void refreshConsumerEntities(boolean realtime) {
        ConcurrentSkipListSet<ConsumerEntity> consumerSet = new ConcurrentSkipListSet<>(ConsumerEntity::compareTo);

        getGroups().forEach(item -> {
            Map<TopicPartition, OffsetAndMetadata> tmp = null;
            try {
                tmp = management.getTopicPartition(item.groupId()).get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.warn("listConsumerGroupOffsets:" + e.getMessage(), e);
            }
            if (!CollectionUtils.isEmpty(tmp)) {
                refreshConsumerEntities(item, tmp, realtime ? null : consumerSet);
            }
        });
        if (!realtime) {
            if (consumerSet.isEmpty()) {
                consumers.clear();
            } else {
                resetConsumerEntities(consumerSet, (item, target) -> item.setOffsetAndMetadata(target.getOffsetAndMetadata())
						.setEndOffset(target.getEndOffset()));
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
        management.getEndOffsets(data.keySet()).forEach((tp, l) -> {
            ConsumerEntity source = new ConsumerEntity(group, tp);
            ConsumerEntity target = consumers.ceiling(source);

            if (target != null && source.compareTo(target) == 0) {
                target.setOffsetAndMetadata(data.get(tp)).setEndOffset(l);
            }
        });
    }

    protected void resetConsumerEntities(ConsumerGroupListing group, Map<TopicPartition, OffsetAndMetadata> data,
										 ConcurrentSkipListSet<ConsumerEntity> consumers) {
        management.getEndOffsets(data.keySet()).forEach((tp, l) -> addConsumerEntities(
        		new ConsumerEntity(group, tp, data.get(tp), l), consumers));
    }

    private void addConsumerEntities(ConsumerEntity entity, ConcurrentSkipListSet<ConsumerEntity> consumerSet) {
        if (!consumers.contains(entity)) {
            consumers.add(entity);
        }
		consumerSet.add(entity);
    }

    private void resetConsumerEntities(boolean realtime) {
        ConcurrentSkipListSet<ConsumerEntity> consumerSet = new ConcurrentSkipListSet<>(ConsumerEntity::compareTo);

        synchronized (nodes) {
			nodes.clear();
            management.describedGroups(getGroupIds()).values().parallelStream().forEach(item -> {
                ConsumerGroupDescription description = null;
                try {
					description = item.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.warn("getGroupDescription:" + e.getMessage(), e);
                }
                if (description != null) {
                    ConsumerGroupListing group = new ConsumerGroupListing(description.groupId(), description.isSimpleConsumerGroup());
					nodes.add(new NodeEntity(description.coordinator(), group, description.state()));

                    if (realtime) {
                        resetConsumerEntities(description, group, consumerSet);
                    }
                }
            });
        }
        if (realtime) {
            if (consumerSet.isEmpty()) {
                consumers.clear();
            } else {
                resetConsumerEntities(consumerSet, (item, target) -> item.setHost(target.getHost()));
            }
        }
    }

    protected void resetConsumerEntities(ConsumerGroupDescription description, ConsumerGroupListing group,
										 ConcurrentSkipListSet<ConsumerEntity> consumers) {
        description.members().forEach(item -> item.assignment().topicPartitions().forEach(
        		tp -> addConsumerEntities(new ConsumerEntity(item.consumerId(), item.host(), group, tp), consumers)));
    }

    private void resetConsumerEntities(ConcurrentSkipListSet<ConsumerEntity> consumerSet, BiConsumer<ConsumerEntity, ConsumerEntity> consumer) {
        consumers.forEach(item -> {
            ConsumerEntity target = consumerSet.ceiling(item);

            if (target != null && item.compareTo(target) == 0) {
                consumer.accept(item, target);
            } else {
                consumers.remove(item);
            }
        });
    }

    public final ConsumerEntity[] consumerCache() {
        return consumers.stream().map(ConsumerEntity::clone).toArray(ConsumerEntity[]::new);
    }

    public final NodeEntity[] nodeCache() {
        synchronized (nodes) {
            return nodes.stream().map(NodeEntity::clone).toArray(NodeEntity[]::new);
        }
    }

    @Override
    public void destroy(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            scheduled.shutdownNow();
        } else {
            scheduled.shutdown();
        }
        SystemUtils.destroy(management, timeout);
        LOGGER.info("SimpleMonitor destroyed,timeout=" + timeout);
    }
}

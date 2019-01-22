package org.pizazz.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.common.IOUtils;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.kafka.exception.CodeEnum;
import org.pizazz.kafka.exception.KafkaException;
import org.pizazz.kafka.support.AbstractClient;

public class Management<K, V> extends AbstractClient {
	private AdminClient admin;
	private KafkaConsumer<K, V> consumer;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		super.initialize(config);
		admin = KafkaAdminClient.create(getConvertor().kafkaConfig());
		consumer = new KafkaConsumer<K, V>(getConvertor().kafkaConfig());
	}

	public ConsumerRecords<K, V> getRecords(TopicPartition tp, long offset) {
		if (offset == -1) {
			consumer.seekToEnd(Arrays.asList(tp));
		} else {
			consumer.seek(tp, offset);
		}
		return consumer.poll(getConvertor().durationValue());
	}

	public Map<String, KafkaFuture<ConsumerGroupDescription>> describedGroups(Collection<String> groupIds) {
		return admin.describeConsumerGroups(groupIds).describedGroups();
	}

	public Map<String, ConsumerGroupDescription> describedGroupsSync(Collection<String> groupIds)
			throws KafkaException {
		try {
			return admin.describeConsumerGroups(groupIds).all().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new KafkaException(CodeEnum.KFK_0014, e.getMessage(), e);
		}
	}

	public Map<TopicPartition, Long> getEndOffsets(Collection<TopicPartition> partitions) {
		return consumer.endOffsets(partitions);
	}

	public KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> getTopicPartition(String groupId) {
		return admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata();
	}

	public ListConsumerGroupsResult getGroups() {
		return admin.listConsumerGroups();
	}

	public KafkaFuture<Void> createTopic(String topic, int partition, int replicationFactor) {
		NewTopic _tmp = new NewTopic(topic, partition, new Integer(replicationFactor).shortValue());
		Map<String, KafkaFuture<Void>> _result = admin.createTopics(Arrays.asList(_tmp)).values();
		return _result.containsKey(topic) ? _result.get(topic) : null;
	}

	public KafkaFuture<Void> deleteTopic(String topic) {
		Map<String, KafkaFuture<Void>> _result = admin.deleteTopics(Arrays.asList(topic)).values();
		return _result.containsKey(topic) ? _result.get(topic) : null;
	}

	public KafkaFuture<Void> refreshPartition(String topic, int partition) {
		Map<String, NewPartitions> _tmp = new HashMap<String, NewPartitions>();
		_tmp.put(topic, NewPartitions.increaseTo(partition));
		Map<String, KafkaFuture<Void>> _result = admin.createPartitions(_tmp).values();
		return _result.containsKey(topic) ? _result.get(topic) : null;
	}

	public AdminClient getTarget() {
		return admin;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		super.destroy(timeout);
		consumer.wakeup();
		IOUtils.close(consumer);
		IOUtils.close(admin);
	}
}

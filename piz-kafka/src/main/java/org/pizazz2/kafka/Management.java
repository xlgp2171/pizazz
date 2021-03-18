package org.pizazz2.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.kafka.exception.CodeEnum;
import org.pizazz2.kafka.exception.KafkaException;
import org.pizazz2.kafka.support.AbstractClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * kafka管理组件
 *
 * @param <K> 消息Key
 * @param <V> 消息Value
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class Management<K, V> extends AbstractClient {
    private AdminClient admin;
    private KafkaConsumer<K, V> consumer;

    public Management(TupleObject configure) throws ValidateException, BaseException {
        super(configure);
    }

    @Override
    protected void setUpConfig() throws ValidateException, BaseException {
        super.setUpConfig();
        admin = KafkaAdminClient.create(getConvertor().kafkaConfig());
        consumer = new KafkaConsumer<>(getConvertor().kafkaConfig());
    }

    public ConsumerRecords<K, V> getRecords(TopicPartition tp, long offset) {
        List<TopicPartition> tmp = Collections.singletonList(tp);
        consumer.assign(tmp);

        if (offset == -1) {
            consumer.seekToEnd(tmp);
        } else {
            consumer.seek(tp, offset);
        }
        return consumer.poll(getConvertor().durationValue());
    }

    public Map<String, KafkaFuture<TopicDescription>> describeTopics(Collection<String> topics) {
        return admin.describeTopics(topics).values();
    }

    public Map<String, KafkaFuture<ConsumerGroupDescription>> describedGroups(Collection<String> groupIds) {
        return admin.describeConsumerGroups(groupIds).describedGroups();
    }

    public Map<String, ConsumerGroupDescription> describedGroupsSync(Collection<String> groupIds) throws KafkaException {
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
        NewTopic tmp = new NewTopic(topic, partition, new Integer(replicationFactor).shortValue());
        Map<String, KafkaFuture<Void>> result = admin.createTopics(Collections.singletonList(tmp)).values();
        return result.getOrDefault(topic, null);
    }

    public KafkaFuture<Void> deleteTopic(String topic) {
        Map<String, KafkaFuture<Void>> result = admin.deleteTopics(Collections.singletonList(topic)).values();
        return result.getOrDefault(topic, null);
    }

    public KafkaFuture<Void> refreshPartition(String topic, int partition) {
        Map<String, NewPartitions> tmp = new HashMap<>();
        tmp.put(topic, NewPartitions.increaseTo(partition));
        Map<String, KafkaFuture<Void>> result = admin.createPartitions(tmp).values();
        return result.getOrDefault(topic, null);
    }

    public AdminClient getTarget() {
        return admin;
    }

    @Override
    public void destroy(Duration timeout) {
        super.destroy(timeout);
        consumer.wakeup();
        consumer.close(timeout);
        admin.close(timeout);
    }
}

package org.pizazz.kafka.monitor;

import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.pizazz.IObject;
import org.pizazz.common.JSONUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.exception.BaseException;

public final class ConsumerEntity implements IObject, Comparable<ConsumerEntity> {
	/** 消费者ID */
	private final String id;
	/** 消费者主机 */
	private String host;
	/** 消费者组 */
	private final ConsumerGroupListing group;
	/** 主题和分区 */
	private final TopicPartition topicPartition;
	/** 提交偏移和元数据 */
	private OffsetAndMetadata metadata;
	/** 最终偏移 */
	private long endOffset;

	public ConsumerEntity(String groupId, TopicPartition topicPartition) {
		this(null, null, new ConsumerGroupListing(groupId, false), topicPartition);
	}

	public ConsumerEntity(String id, String host, ConsumerGroupListing group, TopicPartition topicPartition) {
		this.id = id;
		this.host = host;
		this.group = group;
		this.topicPartition = topicPartition;
	}

	@Override
	public String getId() {
		return id;
	}

	public ConsumerEntity setHost(String host) {
		this.host = host;
		return this;
	}

	public String getHost() {
		return host;
	}

	public String getGroupId() {
		return group == null ? StringUtils.EMPTY : group.groupId();
	}

	public boolean isSimpleConsumerGroup() {
		return group == null ? true : group.isSimpleConsumerGroup();
	}

	public int getPartition() {
		return topicPartition == null ? -1 : topicPartition.partition();
	}

	public String getTopic() {
		return topicPartition == null ? StringUtils.EMPTY : topicPartition.topic();
	}

	public ConsumerEntity setOffsetAndMetadata(OffsetAndMetadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public long getOffset() {
		return metadata == null ? -1L : metadata.offset();
	}

	public String getMetadata() {
		return metadata == null ? StringUtils.EMPTY : metadata.metadata();
	}

	public ConsumerEntity setEndOffset(long endOffset) {
		this.endOffset = endOffset;
		return this;
	}

	public long getEndOffset() {
		return endOffset;
	}

	@Override
	public ConsumerEntity clone() {
		synchronized (this) {
			return new ConsumerEntity(id, host, group, topicPartition).setOffsetAndMetadata(metadata)
					.setEndOffset(endOffset);
		}
	}

	@Override
	public String toString() {
		try {
			return JSONUtils.toJSON(this);
		} catch (BaseException e) {
			return e.getMessage();
		}
	}

	@Override
	public int compareTo(ConsumerEntity o) {
		int _result = getGroupId().compareTo(o.getGroupId());

		if (_result != 0) {
			return _result;
		}
		_result = getTopic().compareTo(o.getTopic());

		if (_result != 0) {
			return _result;
		} else if (getPartition() == o.getPartition()) {
			return 0;
		}
		return getPartition() < o.getPartition() ? -1 : 1;
	}
}

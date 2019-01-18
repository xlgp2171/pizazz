package org.pizazz.kafka.monitor;

import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.pizazz.IObject;
import org.pizazz.common.StringUtils;

public final class NodeEntity implements IObject, Comparable<NodeEntity> {

	private final Node node;
	private final ConsumerGroupListing group;
	private final ConsumerGroupState state;

	public NodeEntity(Node node, String groupId) {
		this(node, new ConsumerGroupListing(groupId, false), null);
	}

	public NodeEntity(Node node, ConsumerGroupListing group, ConsumerGroupState state) {
		this.node = node;
		this.group = group;
		this.state = state;
	}

	public String getId() {
		return node == null ? StringUtils.EMPTY : StringUtils.of(node.id());
	}

	public String getGroupId() {
		return group == null ? StringUtils.EMPTY : group.groupId();
	}

	public boolean isSimpleConsumerGroup() {
		return group == null ? true : group.isSimpleConsumerGroup();
	}

	public ConsumerGroupState getState() {
		return state;
	}

	public String getHost() {
		return node == null ? StringUtils.EMPTY : node.host();
	}

	public int getPort() {
		return node == null ? -1 : node.port();
	}

	public String getRack() {
		return node == null ? StringUtils.EMPTY : node.rack();
	}

	@Override
	public NodeEntity clone() {
		synchronized (this) {
			return new NodeEntity(node, group, state);
		}
	}

	@Override
	public int compareTo(NodeEntity o) {
		int _result = getHost().compareTo(o.getHost());

		if (_result != 0) {
			return _result;
		} else if (getPort() == o.getPort()) {
			return getGroupId().compareTo(o.getGroupId());
		}
		return getPort() < o.getPort() ? -1 : 1;
	}
}

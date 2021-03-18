package org.pizazz2.kafka.monitor;

import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.pizazz2.IObject;
import org.pizazz2.common.StringUtils;

/**
 * 节点信息实体
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public final class NodeEntity implements IObject, Comparable<NodeEntity> {

	private Node node;
	private ConsumerGroupListing group;
	private ConsumerGroupState state;

	public NodeEntity(Node node, String groupId) {
		this(node, new ConsumerGroupListing(groupId, false), null);
	}

	public NodeEntity(Node node, ConsumerGroupListing group, ConsumerGroupState state) {
		this.node = node;
		this.group = group;
		this.state = state;
	}

	public void update(NodeEntity entity) {
		this.node = entity.node;
		this.group = entity.group;
		this.state = entity.state;
	}

	@Override
	public String getId() {
		return node == null ? StringUtils.EMPTY : StringUtils.of(node.id());
	}

	public String getGroupId() {
		return group == null ? StringUtils.EMPTY : group.groupId();
	}

	public boolean isSimpleConsumerGroup() {
		return group == null || group.isSimpleConsumerGroup();
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
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeEntity) {
			if (compareTo((NodeEntity) obj) == 0) {
				return true;
			}
		}
		return super.equals(obj);
	}

	@Override
	public NodeEntity clone() {
		synchronized (this) {
			return new NodeEntity(node, group, state);
		}
	}

	@Override
	public int compareTo(NodeEntity o) {
		int result = getHost().compareTo(o.getHost());

		if (result != 0) {
			return result;
		} else if (getPort() == o.getPort()) {
			return getGroupId().compareTo(o.getGroupId());
		}
		return getPort() < o.getPort() ? -1 : 1;
	}

	@Override
	public String toString() {
		// (1@127.0.0.1:80/rack)GROUP[STATE]
		return "(" + getId() + "@" + getHost() + ":" + getPort() + "/" + getRack() + ")" + getGroupId() + "[" + getState() + "]";
	}
}

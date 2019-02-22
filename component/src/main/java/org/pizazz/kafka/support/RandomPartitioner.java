package org.pizazz.kafka.support;

import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;

public class RandomPartitioner extends DefaultPartitioner {

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		// 排除采用keyBytes方式分区
        return super.partition(topic, key, null, value, valueBytes, cluster);
    }
}

package org.pizazz2.kafka.support;

import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;

/**
 * 随机分区
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public class RandomPartitioner extends DefaultPartitioner {

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		// 排除采用keyBytes方式分区
        return super.partition(topic, key, null, value, valueBytes, cluster);
    }
}

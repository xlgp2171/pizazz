package org.pizazz2.kafka.consumer.adapter;

import org.pizazz2.IObject;

/**
 * 连接器
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
@FunctionalInterface
public interface IBridge extends IObject {

	/**
	 * 运行连接器
	 * @throws Exception 连接过程中异常
	 */
	void passing() throws Exception;
}

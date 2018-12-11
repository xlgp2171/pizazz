package org.pizazz;

import org.pizazz.common.SystemUtils;

/**
 * 对象接口<br>
 * 需要对象处理时使用
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface IObject extends Cloneable {

	public default String getId() {
		return SystemUtils.createID(this);
	}
}

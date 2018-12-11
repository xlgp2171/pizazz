package org.pizazz;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;

/**
 * 插件接口
 * @author pizazz
 * @version 1.0.180601
 */
public interface IPlugin extends IObject {

	public void initialize(TupleObject config) throws BaseException;
	public void destroy(int timeout);
}

package org.pizazz;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;

/**
 * 插件接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 * 
 * @see IObject
 * @see ICloseable
 */
public interface IPlugin extends IObject, ICloseable {

	public void initialize(TupleObject config) throws BaseException;
}

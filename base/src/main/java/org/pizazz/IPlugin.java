package org.pizazz;

import org.pizazz.data.TupleObject;
import org.pizazz.exception.AbstractException;

/**
 * 插件接口
 * 
 * @author xlgp2171
 * @version 1.1.190220
 * 
 * @see IObject
 * @see ICloseable
 */
public interface IPlugin extends IObject, ICloseable {

	public void initialize(TupleObject config) throws AbstractException;
}

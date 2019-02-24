package org.pizazz.tool.ref;

import org.pizazz.exception.AssertException;

/**
 * ID生成工厂接口
 * 
 * @author xlgp2171
 * @version 1.0.190224
 */
public interface IIdFactory {
	/**
	 * 时间2018-01-01 00:00:00.000毫秒数
	 */
	public static final long ELAPSE = 1514736000000L;

	public IdObject create(short custom) throws AssertException;

	public long generate(IdObject id) throws AssertException;

	public IdObject parse(long id) throws AssertException;
}

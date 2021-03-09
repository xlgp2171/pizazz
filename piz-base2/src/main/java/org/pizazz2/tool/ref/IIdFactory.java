package org.pizazz2.tool.ref;

import org.pizazz2.exception.ValidateException;

/**
 * ID生成工厂接口
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IIdFactory {
	/**
	 * 时间2018-01-01 00:00:00.000毫秒数
	 */
	static final long ELAPSE = 1514736000000L;

	/**
	 * 根据自定义序号创建Id对象
	 * @param custom 自定义序号
	 * @return Id对象
	 * @throws ValidateException 自定义序号验证异常
	 */
	IdObject create(short custom) throws ValidateException;

	/**
	 * 生成id
	 * @param id id对象
	 * @return id号
	 * @throws ValidateException 验证异常
	 */
	long generate(IdObject id) throws ValidateException;

	/**
	 * 解析id对象
	 * @param id id号
	 * @return id对象
	 * @throws ValidateException 验证异常
	 */
	IdObject parse(long id) throws ValidateException;
}

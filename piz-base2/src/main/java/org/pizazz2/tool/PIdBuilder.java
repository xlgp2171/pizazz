package org.pizazz2.tool;

import org.pizazz2.exception.ValidateException;
import org.pizazz2.tool.ref.IIdFactory;
import org.pizazz2.tool.ref.IdObject;

/**
 * 通用ID生成组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PIdBuilder {
	private final IIdFactory factory;
	private final short custom;

	public PIdBuilder(IIdFactory factory, short custom) {
		this.factory = factory;
		this.custom = custom;
	}

	public long generate() throws ValidateException {
		IdObject tmp = factory.create(custom);
		return factory.generate(tmp);
	}
}

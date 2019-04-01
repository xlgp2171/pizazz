package org.pizazz.tool;

import org.pizazz.exception.AssertException;
import org.pizazz.tool.ref.IIdFactory;
import org.pizazz.tool.ref.IdObject;

/**
 * 通用ID生成组件
 * 
 * @author xlgp2171
 * @version 1.0.190224
 */
public class PIdBuilder {
	private final IIdFactory factory;
	private final short custom;

	public PIdBuilder(IIdFactory factory) {
		this(factory, (short) 0);
	}

	public PIdBuilder(IIdFactory factory, short custom) {
		this.factory = factory;
		this.custom = custom;
	}

	public long generate() throws AssertException {
		return generate(custom);
	}

	public long generate(short custom) throws AssertException {
		IdObject _tmp = factory.create(custom);
		return factory.generate(_tmp);
	}
}

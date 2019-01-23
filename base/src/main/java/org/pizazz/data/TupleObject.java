package org.pizazz.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.pizazz.Constant;
import org.pizazz.IObject;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.message.ErrorCodeEnum;

/**
 * 通用对象
 * 
 * @author xlgp2171
 * @version 1.0.190122
 */
public class TupleObject extends LinkedHashMap<String, Object> implements IObject {
	private static final long serialVersionUID = 2892973669901268754L;
	private String id;

	public TupleObject() {
		super();
	}

	public TupleObject(int size) {
		super(size);
	}

	public TupleObject(Map<String, ? extends Object> target) {
		super(target);
	}

	public TupleObject(TupleObject target) {
		super(target);
	}

	public TupleObject append(String key, Object value) {
		put(key, value);
		return this;
	}

	public TupleObject append(Map<String, ? extends Object> value) {
		if (value != null) {
			putAll(value);
		}
		return this;
	}

	@Override
	public String getId() {
		// 默认的ID为:piz@[UUID]
		if (id == null) {
			synchronized (this) {
				if (id == null) {
					id = new StringBuilder(Constant.NAMING_SHORT).append("@").append(SystemUtils.newUUID()).toString();
				}
			}
		}
		return id;
	}

	public Map<String, Object> asMap() {
		return this;
	}

	@Override
	public TupleObject clone() {
		try {
			return ClassUtils.cast(super.clone(), TupleObject.class);
		} catch (BaseException e) {
			throw new BaseError(ErrorCodeEnum.ERR_0002, e);
		}
	}
}

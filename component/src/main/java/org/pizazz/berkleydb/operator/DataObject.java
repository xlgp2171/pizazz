package org.pizazz.berkleydb.operator;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.BaseException;

public class DataObject<T> {
	private byte[] key;
	private T value;

	public DataObject(byte[] key, T value) throws BaseException {
		AssertUtils.assertNotNull("DataObject", key, value);
		this.key = key;
		this.value = value;
	}

	public byte[] getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}
}

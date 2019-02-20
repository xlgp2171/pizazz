package org.pizazz.berkleydb.operator;

import org.pizazz.common.AssertUtils;
import org.pizazz.exception.AssertException;

public class DataObject<T> {
	private byte[] key;
	private T value;

	public DataObject(byte[] key, T value) throws AssertException {
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

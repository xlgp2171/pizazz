package org.pizazz2.data;

import org.pizazz2.IObject;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应对象
 * 
 * @author xlgp2171
 * @version 2.0.211111
 */
public class ResponseObject<P, T> implements IObject {
	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;

	private final int code;
	private final Map<String, P> properties = new HashMap<>();
	private final String message;
	private T result;

	public ResponseObject(int code) {
		this(code, null, null, null);
	}

	public ResponseObject(int code, String message) {
		this(code, null, message, null);
	}

	public ResponseObject(int code, Map<String, P> properties) {
		this(code, properties, null, null);
	}

	public ResponseObject(int code, Map<String, P> properties, String message, T result) {
		this.code = code;
		this.result = result;
		this.message = message;

		if (!CollectionUtils.isEmpty(properties)) {
			this.properties.putAll(properties);
		}
	}

	@Override
	public String getId() {
		return StringUtils.of(code);
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty() && result == null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(String key, Object target) {
		properties.put(key, (P) target);
	}

	@Override
	public void reset() {
		setResult(null);
		properties.clear();
	}

	@Override
	public Object get(String key, Object defValue) {
		if (properties.containsKey(key)) {
			return properties.get(key);
		}
		return defValue;
	}

	@Override
	public IObject copy() {
		return new ResponseObject<>(code, properties, message, result);
	}

	public int getCode() {
		return code;
	}

	public ResponseObject<P, T> setResult(T result) {
		this.result = result;
		return this;
	}

	public T getResult() {
		return result;
	}

	public Map<String, P> getProperties() {
		return properties;
	}

	public ResponseObject<P, T> setProperty(String key, P value) {
		if (value != null) {
			properties.put(key, value);
		}
		return this;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return code + "#" + StringUtils.nullToEmpty(message);
	}
}

package org.pizazz2.tool.ref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.StringUtils;

/**
 * 响应对象
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ResponseObject {

	private final int code;
	private final Map<String, List<String>> properties = new HashMap<>();
	private final byte[] data;

	public ResponseObject(int code, byte[] data, Map<String, List<String>> properties) {
		this.code = code;
		this.data = data;

		if (!CollectionUtils.isEmpty(properties)) {
			this.properties.putAll(properties);
		}
	}

	public int getCode() {
		return code;
	}

	public byte[] getData() {
		return data;
	}

	public Map<String, List<String>> getProperties() {
		return properties;
	}

	public ResponseObject setProperty(String key, String[] value) {
		if (!ArrayUtils.isEmpty(value)) {
			properties.put(key, Arrays.asList(value));
		}
		return this;
	}

	@Override
	public String toString() {
		String tmp = StringUtils.EMPTY;

		if (!ArrayUtils.isEmpty(data)) {
			tmp = new String(data);
		}
		return code + "," + tmp;
	}
}

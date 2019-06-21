package org.pizazz.tool.ref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pizazz.common.ArrayUtils;
import org.pizazz.common.CollectionUtils;

/**
 * 响应对象
 * 
 * @author xlgp2171
 * @version 1.0.190617
 */
public class ResponseObject {

	private final int code;
	private final Map<String, List<String>> properties = new HashMap<String, List<String>>();
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
		String _tmp = "";

		if (!ArrayUtils.isEmpty(data)) {
			_tmp = new String(data);
		}
		return code + "," + _tmp;
	}
}

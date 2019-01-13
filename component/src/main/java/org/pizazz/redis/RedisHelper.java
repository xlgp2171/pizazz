package org.pizazz.redis;

import java.nio.charset.StandardCharsets;

import org.pizazz.common.BytesUtils;

public class RedisHelper {

	public static String toString(byte[] target) {
		return BytesUtils.toString(target, StandardCharsets.UTF_8);
	}

	public static byte[] fromString(String target) {
		return BytesUtils.toByteArray(target, StandardCharsets.UTF_8);
	}
}

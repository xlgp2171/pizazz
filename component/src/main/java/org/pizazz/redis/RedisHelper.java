package org.pizazz.redis;

import java.nio.charset.StandardCharsets;

public class RedisHelper {

	public static String toString(byte[] target) {
		return new String(target, StandardCharsets.UTF_8);
	}

	public static byte[] fromString(String target) {
		return target.getBytes(StandardCharsets.UTF_8);
	}
}

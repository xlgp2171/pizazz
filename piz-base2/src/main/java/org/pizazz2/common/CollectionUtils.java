package org.pizazz2.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;

/**
 * 集合工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class CollectionUtils {

	public static boolean isEmpty(Collection<?> target) {
		return target == null || target.isEmpty();
	}

	public static boolean isEmpty(Map<?, ?> target) {
		return target == null || target.isEmpty();
	}

	public static <T> List<T> emptyList() {
		return Collections.emptyList();
	}

	public static <K, V> Map<K, V> emptyMap() {
		return Collections.emptyMap();
	}

	public static <T> Set<T> emptySet() {
		return Collections.emptySet();
	}

	public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> target) {
		return Collections.unmodifiableMap(target);
	}

	public static <T> Set<T> unmodifiableSet(Set<? extends T> target) {
		return Collections.unmodifiableSet(target);
	}

	public static <E> HashSet<E> asHashSet(E[] element) {
		if (ArrayUtils.isEmpty(element)) {
			return new LinkedHashSet<>();
		}
		HashSet<E> tmp = new LinkedHashSet<>(element.length);
		Collections.addAll(tmp, element);
		return tmp;
	}

	public static <E> void merge(Set<E> target, E[] element) throws ValidateException {
		if (ArrayUtils.isEmpty(element)) {
			return;
		}
		ValidateUtils.notNull("merge", target);
		Collections.addAll(target, element);
	}

	/**
	 * Key和Value反转
	 * 
	 * @param target 目标Map
	 * @return 反转后的Map
	 * @throws ValidateException 验证参数为空
	 */
	public static <K, V> Map<V, K> flip(Map<K, V> target) throws ValidateException {
		ValidateUtils.notNull("flip", target);
		Map<V, K> tmp = new HashMap<>(target.size());

		for (Map.Entry<K, V> item : target.entrySet()) {
			tmp.put(item.getValue(), item.getKey());
		}
		return tmp;
	}

	@SuppressWarnings("unchecked")
	public static List<String> convert(List<Object> target) {
		if (CollectionUtils.isEmpty(target)) {
			return CollectionUtils.emptyList();
		}
		List<String> tmp;
		try {
			// 用于保持输入集合的类型不变
			tmp = ClassUtils.newAndCast(target.getClass(), List.class);
		} catch (UtilityException e) {
			tmp = new LinkedList<>();
		}
		for (Object item : target) {
			tmp.add(StringUtils.of(item));
		}
		return tmp;
	}

	public static String toString(Collection<?> target) {
		if (CollectionUtils.isEmpty(target)) {
			return "[]";
		}
		StringBuilder tmp = new StringBuilder("[");

		for (Object item : target) {
			tmp.append(item).append(",");
		}
		return tmp.deleteCharAt(tmp.length() - 1).append("]").toString();
	}
}

package org.pizazz.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pizazz.exception.BaseException;

/**
 * 集合工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
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

	public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> target) {
		return Collections.unmodifiableMap(target);
	}

	public static <T> Set<T> unmodifiableSet(Set<? extends T> target) {
		return Collections.unmodifiableSet(target);
	}

	public static <E> HashSet<E> asHashSet(E[] element) {
		if (ArrayUtils.isEmpty(element)) {
			return new LinkedHashSet<E>();
		}
		HashSet<E> _tmp = new LinkedHashSet<E>(element.length);

		for (E _item : element) {
			_tmp.add(_item);
		}
		return _tmp;
	}

	public static <E> void merge(Set<E> target, E[] element) throws BaseException {
		if (ArrayUtils.isEmpty(element)) {
			return;
		}
		AssertUtils.assertNotNull("merge", target);

		for (E _item : element) {
			target.add(_item);
		}
	}

	/**
	 * Key和Value反转
	 * 
	 * @param target
	 * @return
	 * @throws BaseException
	 */
	public static <K, V> Map<V, K> flip(Map<K, V> target) throws BaseException {
		AssertUtils.assertNotNull("flip", target);
		Map<V, K> _tmp = new HashMap<V, K>();

		for (Map.Entry<K, V> _item : target.entrySet()) {
			_tmp.put(_item.getValue(), _item.getKey());
		}
		return _tmp;
	}

	@SuppressWarnings("unchecked")
	public static List<String> convert(List<Object> target) {
		if (isEmpty(target)) {
			return emptyList();
		}
		List<String> _tmp;
		try {
			_tmp =  ClassUtils.newAndCast(target.getClass(), List.class);
		} catch (BaseException e) {
			_tmp = new LinkedList<String>();
		}
		for (Object _item : target) {
			_tmp.add(StringUtils.of(_item));
		}
		return _tmp;
	}
}

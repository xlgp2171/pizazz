package org.pizazz2.common;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.convert.Convert;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;

/**
 * 集合工具
 * 
 * @author xlgp2171
 * @version 2.2.230323
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
	public static List<String> convert(List<?> target) {
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

	public static List<?> toList(Object target) {
		if (!(target instanceof List)) {
			return new ArrayList<>(Collections.singletonList(target));
		}
		return (List<?>) target;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(Object target, Class<T> clazz)  {
		ValidateUtils.notNull("target", clazz);

		if (ObjectUtils.isNull(target)) {
			return CollectionUtils.emptyList();
		}
		Class<?> targetClass = target.getClass();
		Collection<Object> tmp;
		// 按集合和数组方式组织数据
		if (targetClass.isArray()) {
			tmp = CollectionUtils.arrayToCollection(target);
		} else if (targetClass.isAssignableFrom(Collection.class)) {
			tmp = new ArrayList<>(((Collection<Object>) target));
		} else {
			// 若不是集合或者数组
			tmp = new ArrayList<>(Collections.singletonList(target));
		}
		return tmp.stream().map(item -> ObjectUtils.convertPrimitive(item, clazz)).collect(Collectors.toList());
	}

	static Collection<Object> arrayToCollection(Object target) {
		List<Object> result = new ArrayList<>();
		Class<?> clazz = target.getClass();

		if (clazz == int[].class) {
			for (Object item : (int[]) target) {
				result.add(item);
			}
		} else if (clazz == byte[].class) {
			for (Object item : (byte[]) target) {
				result.add(item);
			}
		} else if (clazz == short[].class) {
			for (Object item : (short[]) target) {
				result.add(item);
			}
		} else if (clazz == double[].class) {
			for (Object item : (double[]) target) {
				result.add(item);
			}
		} else if (clazz == float[].class) {
			for (Object item : (float[]) target) {
				result.add(item);
			}
		} else if (clazz == long[].class) {
			for (Object item : (long[]) target) {
				result.add(item);
			}
		} else if (clazz == boolean[].class) {
			for (Object item : (boolean[]) target) {
				result.add(item);
			}
		} else {
			result.addAll(Arrays.asList((Object[]) target));
		}
		return result;
	}

	public static Set<?> toSet(Object target) {
		if (!(target instanceof List)) {
			return new HashSet<>();
		}
		return (Set<?>) target;
	}

	public static Collection<?> toCollection(Object target) {
		if (!(target instanceof Collection)) {
			return new LinkedList<>();
		}
		return (Collection<?>) target;
	}

	/**
	 * 随机分布元素
	 * @param target 目标集合
	 * @param seed 随机种子
	 * @throws ValidateException 验证参数为空
	 */
	public static void shuffle(List<?> target, long seed) throws ValidateException {
		ValidateUtils.notNull("shuffle", target);
		Collections.shuffle(target, new Random(seed));
	}

	/**
	 * 反转集合
	 * @param target 目标集合
	 * @throws ValidateException 验证参数为空
	 */
	public static void reverse(List<?> target) throws ValidateException {
		ValidateUtils.notNull("reverse", target);
		Collections.reverse(target);
	}

	/**
	 * 将数Map换为Properties<br>
	 * 若要正确使用，值需为String类型
	 * @param target 目标对象
	 * @return Properties对象
	 */
	public static Properties asProperties(Map<String, ?> target) {
		Properties tmp = new Properties();

		if (!CollectionUtils.isEmpty(target)) {
			tmp.putAll(target);
		}
		return tmp;
	}
}

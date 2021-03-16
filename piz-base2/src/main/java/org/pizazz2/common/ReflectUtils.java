package org.pizazz2.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 反射工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ReflectUtils {

	public static String getPackageName(Class<?> clazz) throws ValidateException {
		ValidateUtils.notNull("getPackageName", clazz);
		return clazz.getPackage().getName();
	}

	public static <T> T invokeConstructor(Class<T> clazz, Object... arguments)
			throws ValidateException, UtilityException {
		Class<?>[] types = ClassUtils.toClass(false, arguments);
		return ReflectUtils.invokeConstructor(clazz, types, arguments, false);
	}

	public static <T> T invokeConstructor(Class<T> clazz, Class<?>[] types, Object[] arguments, boolean isDeclared)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeConstructor", clazz);
		types = ArrayUtils.nullToEmpty(types);
		arguments = ArrayUtils.nullToEmpty(arguments);
		Constructor<T> constructor;
		try {
			constructor = clazz.getConstructor(types);
		} catch (NoSuchMethodException | SecurityException e1) {
			if (!isDeclared) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e1.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e1);
			}
			try {
				constructor = clazz.getDeclaredConstructor(types);
			} catch (NoSuchMethodException | SecurityException e2) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e2.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e2);
			}
		}
		try {
			return constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR.INIT", Arrays.toString(types),
					clazz.getName(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		}
	}

	public static <T> T invokeMethod(Object target, String methodName, Class<?>[] types, Object[] arguments,
			Class<T> returnType, boolean accessible) throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeMethod", target);
		Method method = getMethod(target instanceof Class ? (Class<?>) target : target.getClass(), methodName, types,
				accessible);
		Object tmp = ReflectUtils.invokeMethod(method, target, arguments, accessible);
		return ClassUtils.cast(tmp, returnType);
	}

	public static Object invokeMethod(Method method, Object target, Object[] arguments, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeMethod", method, target);
		arguments = ArrayUtils.nullToEmpty(arguments);
		method.setAccessible(accessible);
		try {
			return method.invoke(target, arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", target.getClass().getName(),
					method.getName(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		} finally {
			method.setAccessible(false);
		}
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] types, boolean isDeclared)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("getMethod", clazz, methodName);
		types = ArrayUtils.nullToEmpty(types);
		try {
			return clazz.getMethod(methodName, types);
		} catch (NoSuchMethodException | SecurityException e1) {
			if (!isDeclared) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", clazz.getName(), methodName,
						e1.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e1);
			}
			try {
				return clazz.getDeclaredMethod(methodName, types);
			} catch (NoSuchMethodException | SecurityException e2) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", clazz.getName(), methodName,
						e2.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e2);
			}
		}
	}

	public static Field getField(String name, Object target, boolean accessible) throws UtilityException {
		Class<?> type = target instanceof Class ? (Class<?>) target : target.getClass();
		try {
			return accessible ? type.getDeclaredField(name) : type.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FIELD.GET", type.getName(), name,
					e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		}
	}

	public static Field[] getFields(Class<?> clazz, boolean isDeclared) throws ValidateException {
		ValidateUtils.notNull("getFields", clazz);

		if (isDeclared) {
			return clazz.getDeclaredFields();
		}
		return clazz.getFields();
	}

	public static Map<Class<?>, Field[]> getAllFields(Class<?> clazz, boolean isDeclared) {
		ValidateUtils.notNull("getAllFields", clazz);
		Map<Class<?>, Field[]> tmp = new LinkedHashMap<>();
		Class<?> current = clazz;

		do {
			tmp.put(current, ReflectUtils.getFields(current, isDeclared));
			current = current.getSuperclass();
		} while (current != Object.class);
		return tmp;
	}

	public static TupleObject invokeGetFields(Object target, boolean accessible) throws ValidateException,
			UtilityException {
		ValidateUtils.notNull("invokeGetFields", target);
		Field[] fields = ReflectUtils.getFields(target instanceof Class ? (Class<?>) target : target.getClass(),
				accessible);
		TupleObject tmp = TupleObjectHelper.newObject(fields.length);

		for (Field item : fields) {
			Object value = ReflectUtils.invokeGetField(item, Modifier.isStatic(item.getModifiers()) ?
					target.getClass() : target, Object.class, accessible);
			tmp.append(item.getName(), value);
		}
		return tmp;
	}

	public static void invokeSetField(String name, Object target, Object value, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeSetField", name, target, value);
		Field field = ReflectUtils.getField(name, target, accessible);
		ReflectUtils.invokeSetField(field, target, value, accessible);
	}

	public static void invokeSetField(Field field, Object target, Object value, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeSetField", field, target);
		field.setAccessible(accessible);
		try {
			field.set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FIELD.SET", field.getName(),
					target.getClass().getName(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		} finally {
			field.setAccessible(false);
		}
	}

	public static <T> T invokeGetField(String name, Object target, Class<T> returnType, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeGetField", name, target, returnType);
		Field field = ReflectUtils.getField(name, target, accessible);
		return ReflectUtils.invokeGetField(field, target, returnType, accessible);
	}

	public static <T> T invokeGetField(Field field, Object target, Class<T> returnType, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeGetField", field, target, returnType);
		field.setAccessible(accessible);
		Object tmp;
		try {
			tmp = field.get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FIELD.GET", target.getClass().getName(),
					field.getName(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		} finally {
			field.setAccessible(false);
		}
		return tmp == null ? null : ClassUtils.cast(tmp, returnType);
	}
}

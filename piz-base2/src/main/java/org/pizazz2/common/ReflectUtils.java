package org.pizazz2.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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
		Class<?>[] _types = ClassUtils.toClass(false, arguments);
		return invokeConstructor(clazz, _types, arguments, false);
	}

	public static <T> T invokeConstructor(Class<T> clazz, Class<?>[] types, Object[] arguments, boolean isDeclared)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeConstructor", clazz);
		types = ArrayUtils.nullToEmpty(types);
		arguments = ArrayUtils.nullToEmpty(arguments);
		Constructor<T> _constructor;
		try {
			_constructor = clazz.getConstructor(types);
		} catch (NoSuchMethodException | SecurityException e1) {
			if (!isDeclared) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e1.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e1);
			}
			try {
				_constructor = clazz.getDeclaredConstructor(types);
			} catch (NoSuchMethodException | SecurityException e2) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e2.getMessage());
				throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e2);
			}
		}
		try {
			return _constructor.newInstance(arguments);
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
		Method _method = getMethod(target instanceof Class ? (Class<?>) target : target.getClass(), methodName, types,
				accessible);
		Object tmp = invokeMethod(_method, target, arguments, accessible);
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
		Class<?> _type = target instanceof Class ? (Class<?>) target : target.getClass();
		try {
			return accessible ? _type.getDeclaredField(name) : _type.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FIELD.GET", _type.getName(), name,
					e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0010, msg, e);
		}
	}

	public static Field[] getFields(Class<?> clazz, boolean isDeclared) throws ValidateException {
		ValidateUtils.notNull("getFields", clazz);

		if (isDeclared) {
			// FIXME 看下是否能获取父类字段
			return clazz.getDeclaredFields();
		}
		return clazz.getFields();
	}

	public static TupleObject invokeGetFields(Object target, boolean accessible) throws ValidateException,
			UtilityException {
		ValidateUtils.notNull("invokeGetFields", target);
		Field[] _fields = ReflectUtils.getFields(target instanceof Class ? (Class<?>) target : target.getClass(),
				accessible);
		TupleObject tmp = TupleObjectHelper.newObject(_fields.length);

		for (Field item : _fields) {
			Object _value = ReflectUtils.invokeGetField(item, Modifier.isStatic(item.getModifiers()) ?
					target.getClass() : target, Object.class, accessible);
			tmp.append(item.getName(), _value);
		}
		return tmp;
	}

	public static void invokeSetField(String name, Object target, Object value, boolean accessible)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("invokeSetField", name, target, value);
		Field _field = ReflectUtils.getField(name, target, accessible);
		ReflectUtils.invokeSetField(_field, target, value, accessible);
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
		Field _field = ReflectUtils.getField(name, target, accessible);
		return ReflectUtils.invokeGetField(_field, target, returnType, accessible);
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

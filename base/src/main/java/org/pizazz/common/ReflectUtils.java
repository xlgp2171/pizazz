package org.pizazz.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ref.TypeEnum;

/**
 * 反射工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class ReflectUtils {

	public static String getPackageName(Class<?> clazz) throws BaseException {
		AssertUtils.assertNotNull("getPackageName", clazz);
		return clazz.getPackage().getName();
	}

	public static <T> T invokeConstructor(Class<T> clazz, Object... arguments) throws BaseException {
		Class<?>[] _types = ClassUtils.toClass(false, arguments);
		return invokeConstructor(clazz, _types, arguments, false);
	}

	public static <T> T invokeConstructor(Class<T> clazz, Class<?>[] types, Object[] arguments, boolean isDeclared)
			throws BaseException {
		AssertUtils.assertNotNull("invokeConstructor", clazz);
		types = ArrayUtils.nullToEmpty(types);
		arguments = ArrayUtils.nullToEmpty(arguments);
		Constructor<T> _constructor;
		try {
			_constructor = clazz.getConstructor(types);
		} catch (NoSuchMethodException | SecurityException e1) {
			if (!isDeclared) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e1.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e1);
			}
			try {
				_constructor = clazz.getDeclaredConstructor(types);
			} catch (NoSuchMethodException | SecurityException e2) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR", Arrays.toString(types),
						clazz.getName(), e2.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e2);
			}
		}
		try {
			return _constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CONSTRUCTOR.INIT", Arrays.toString(types),
					clazz.getName(), e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e);
		}
	}

	public static <T> T invokeMethod(Object target, String methodName, Class<?>[] types, Object[] arguments,
			Class<T> returnType, boolean accessible) throws BaseException {
		AssertUtils.assertNotNull("invokeMethod", target);
		Method _method = getMethod(target instanceof Class ? (Class<?>) target : target.getClass(), methodName, types,
				accessible);
		Object _tmp = invokeMethod(_method, target, arguments, accessible);
		return ClassUtils.cast(_tmp, returnType);
	}

	public static Object invokeMethod(Method method, Object target, Object[] arguments, boolean accessible)
			throws BaseException {
		AssertUtils.assertNotNull("invokeMethod", method, target);
		arguments = ArrayUtils.nullToEmpty(arguments);
		method.setAccessible(accessible);
		try {
			return method.invoke(target, arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", target.getClass().getName(),
					method.getName(), e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e);
		} finally {
			method.setAccessible(false);
		}
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] types, boolean isDeclared)
			throws BaseException {
		AssertUtils.assertNotNull("getMethod", clazz, methodName);
		types = ArrayUtils.nullToEmpty(types);
		try {
			return clazz.getMethod(methodName, types);
		} catch (NoSuchMethodException | SecurityException e1) {
			if (!isDeclared) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", clazz.getName(), methodName,
						e1.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e1);
			}
			try {
				return clazz.getDeclaredMethod(methodName, types);
			} catch (NoSuchMethodException | SecurityException e2) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.METHOD", clazz.getName(), methodName,
						e2.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e2);
			}
		}
	}

	public static Field[] getFields(Class<?> clazz, boolean isDeclared) throws BaseException {
		AssertUtils.assertNotNull("getFields", clazz);

		if (isDeclared) {
			return clazz.getDeclaredFields();
		}
		return clazz.getFields();
	}

	public static Map<String, Object> invokeFields(Object target, boolean accessible) throws BaseException {
		AssertUtils.assertNotNull("invokeFields", target);
		Field[] _fields = getFields(target instanceof Class ? (Class<?>) target : target.getClass(), accessible);
		Map<String, Object> _tmp = new HashMap<String, Object>();

		for (Field _item : _fields) {
			Object _value = invokeField(_item, Modifier.isStatic(_item.getModifiers()) ? target.getClass() : target,
					Object.class, accessible);
			_tmp.put(_item.getName(), _value);
		}
		return _tmp;
	}

	public static <T> T invokeField(Field field, Object target, Class<T> returnType, boolean accessible)
			throws BaseException {
		AssertUtils.assertNotNull("invokeField", field, target, returnType);
		field.setAccessible(accessible);
		Object _tmp;
		try {
			_tmp = field.get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FIELD.REF", target.getClass().getName(),
					field.getName(), e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0010, _msg, e);
		} finally {
			field.setAccessible(false);
		}
		return ClassUtils.cast(_tmp, returnType);
	}
}

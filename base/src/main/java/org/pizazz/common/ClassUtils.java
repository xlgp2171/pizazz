package org.pizazz.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.pizazz.Constant;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ExpressionConstant;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;

/**
 * 类处理工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class ClassUtils {
	// 包装类型对应包装类
	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER;
	// 包装类对应包装类型
	private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE;
	// 缩写类型
	private static final Map<String, Character> ABBREVIATION;

	static {
		Map<Class<?>, Class<?>> _tmpClass = new HashMap<Class<?>, Class<?>>();
		_tmpClass.put(Boolean.TYPE, Boolean.class);
		_tmpClass.put(Byte.TYPE, Byte.class);
		_tmpClass.put(Character.TYPE, Character.class);
		_tmpClass.put(Short.TYPE, Short.class);
		_tmpClass.put(Integer.TYPE, Integer.class);
		_tmpClass.put(Long.TYPE, Long.class);
		_tmpClass.put(Double.TYPE, Double.class);
		_tmpClass.put(Float.TYPE, Float.class);
		_tmpClass.put(Void.TYPE, Void.TYPE);
		PRIMITIVE_WRAPPER = CollectionUtils.unmodifiableMap(_tmpClass);
		//
		try {
			_tmpClass = CollectionUtils.flip(PRIMITIVE_WRAPPER);
		} catch (BaseException e) {
			// 忽略空值检查
		}
		WRAPPER_PRIMITIVE = CollectionUtils.unmodifiableMap(_tmpClass);
		//
		Map<String, Character> _tmpAbbreviate = new HashMap<String, Character>();
		_tmpAbbreviate.put("int", 'I');
		_tmpAbbreviate.put("boolean", 'Z');
		_tmpAbbreviate.put("float", 'F');
		_tmpAbbreviate.put("long", 'J');
		_tmpAbbreviate.put("short", 'S');
		_tmpAbbreviate.put("byte", 'B');
		_tmpAbbreviate.put("double", 'D');
		_tmpAbbreviate.put("char", 'C');
		_tmpAbbreviate.put("void", 'V');
		ABBREVIATION = CollectionUtils.unmodifiableMap(_tmpAbbreviate);
	}

	/**
	 * 根据代码获取包名称
	 * @param code 代码
	 * @return
	 * @throws BaseException
	 */
	public static String getPackageName(String code) throws BaseException {
		return StringUtils.match(Pattern.compile(ExpressionConstant.PACKAGE_NAME), code, 1);
	}

	/**
	 * 根据代码获取类名称<br>
	 * 类名称为顺序查找的第一个类
	 * 
	 * @param code 代码
	 * @return
	 * @throws BaseException
	 */
	public static String getClassName(String code) throws BaseException {
		return StringUtils.match(Pattern.compile(ExpressionConstant.CLASS_NAME), code, 1);
	}

	/**
	 * 类实例化
	 * 
	 * @param classpath 类路径
	 * @param type 返回类型
	 * @return 类实例
	 * @throws BaseException
	 */
	public static <T> T newClass(String classpath, Class<T> type) throws BaseException {
		return newClass(classpath, null, type);
	}

	/**
	 * 类实例化
	 * 
	 * @param classpath 类路径
	 * @param loader 类加载器
	 * @param type 返回类型
	 * @return 类实例
	 * @throws BaseException
	 */
	public static <T> T newClass(String classpath, ClassLoader loader, Class<T> type) throws BaseException {
		Class<?> _clazz = loadClass(classpath, loader, true);
		try {
			return cast(_clazz.newInstance(), type);
		} catch (InstantiationException | IllegalAccessException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.INIT", _clazz.getName(), e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0001, _msg, e);
		}
	}

	/**
	 * 类加载
	 * 
	 * @param classpath 类路径
	 * @param loader 类加载器
	 * @param initialize 是否初始化类
	 * @return
	 * @throws BaseException
	 */
	public static Class<?> loadClass(String classpath, ClassLoader loader, boolean initialize) throws BaseException {
		if (StringUtils.isTrimEmpty(classpath)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "loadClass", 1);
			throw new BaseException(BasicCodeEnum.MSG_0001, _msg);
		}
		if (loader == null) {
			loader = getClassLoader(Constant.class, Thread.currentThread());
		}
		try {
			if (ABBREVIATION.containsKey(classpath)) {
				return Class.forName("[" + ABBREVIATION.get(classpath), initialize, loader).getComponentType();
			}
			return Class.forName(classpath, initialize, loader);
		} catch (ClassNotFoundException e) {//
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.FOUND", classpath);
			throw new BaseException(BasicCodeEnum.MSG_0009, _msg, e);
		}
	}

	/**
	 * 获取类加载器
	 * 
	 * @param target
	 * @param current 类加载器所在的线程(可选参数)
	 * @return
	 */
	public static ClassLoader getClassLoader(Class<?> target, Thread current) {
		ClassLoader _loader = null;

		if (current != null) {
			try {
				_loader = current.getContextClassLoader();
			} catch (SecurityException e) {
			}
		}
		if (_loader == null && target != null) {
			_loader = target.getClassLoader();
		}
		if (_loader == null) {
			_loader = ClassLoader.getSystemClassLoader();
		}
		if (current != null) {
			current.setContextClassLoader(_loader);
		}
		return _loader;
	}

	/**
	 * 识别对象返回对象类型
	 * 
	 * @param primitive 是否识别包装类,将之转换为基本类
	 * @param arguments 需要识别的对象
	 * @return
	 */
	public static Class<?>[] toClass(boolean primitive, Object... arguments) {
		if (ArrayUtils.isEmpty(arguments)) {
			return ArrayUtils.EMPTY_CLASS;
		}
		Class<?>[] _classtypes = new Class<?>[arguments.length];

		for (int _i = 0; _i < arguments.length; _i++) {
			if (arguments[_i] == null) {
				_classtypes[_i] = null;
			} else if (primitive) {
				try {
					_classtypes[_i] = toPrimitiveClass(arguments[_i]);
				} catch (BaseException e) {
					_classtypes[_i] = null;
				}
			} else {
				_classtypes[_i] = arguments[_i].getClass();
			}
		}
		return _classtypes;
	}

	/**
	 * 识别包装类，将包装类转换为基础类型
	 * 
	 * @param target
	 * @return
	 * @throws BaseException
	 */
	public static Class<?> toPrimitiveClass(Object target) throws BaseException {
		AssertUtils.assertNotNull("toPrimitiveClass", target);
		Class<?> _tmp = target.getClass();

		if (WRAPPER_PRIMITIVE.containsKey(_tmp)) {
			_tmp = WRAPPER_PRIMITIVE.get(_tmp);
		}
		return _tmp;
	}

	/**
	 * 识别基本类，将基本类转换为包装类型
	 * 
	 * @param argument
	 * @return
	 * @throws BaseException
	 */
	public static Class<?> toWrapperClass(Object target) throws BaseException {
		AssertUtils.assertNotNull("toWrapperClass", target);
		Class<?> _tmp = target.getClass();

		if (_tmp.isPrimitive()) {
			_tmp = PRIMITIVE_WRAPPER.get(_tmp);
		}
		return _tmp;
	}

	/**
	 * 获取类的接口列表
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?>[] getInterfaces(Class<?> clazz) {
		if (clazz == null) {
			return ArrayUtils.EMPTY_CLASS;
		}
		HashSet<Class<?>> _tmp = new HashSet<Class<?>>();
		try {
			getInterfaces(clazz, _tmp);
		} catch (BaseException e) {
			// 忽略空值异常
		}
		return _tmp.toArray(ArrayUtils.EMPTY_CLASS);
	}

	/**
	 * 获取类的接口列表
	 * 
	 * @param clazz
	 * @param cache 用于缓存接口对象
	 * @throws BaseException
	 */
	public static void getInterfaces(Class<?> clazz, HashSet<Class<?>> cache) throws BaseException {
		AssertUtils.assertNotNull("getInterfaces", 0, cache);
		while (clazz != null) {
			Class<?>[] _interfaces = clazz.getInterfaces();

			for (Class<?> _item : _interfaces) {
				if (cache.add(_item)) {
					getInterfaces(_item, cache);
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	public static <T> T cast(Object target, Class<T> type) throws BaseException {
		AssertUtils.assertNotNull("cast", target, type);
		try {
			return type.cast(target);
		} catch (ClassCastException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.CAST", target.getClass().getName(),
					type.getName());
			throw new BaseException(BasicCodeEnum.MSG_0004, _msg, e);
		}
	}
}

package org.pizazz2.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.pizazz2.PizContext;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ExpressionEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 类处理工具<br>
 * 参考org.apache.commons.beanutils.BeanUtilsBean
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ClassUtils {
    /**
     * 包装类型对应包装类
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER;
    /**
     * 包装类对应包装类型
     */
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE;
    /**
     * 缩写类型
     */
    private static final Map<String, Character> ABBREVIATION;

    static {
        Map<Class<?>, Class<?>> tmpClass = new HashMap<>(9);
        tmpClass.put(Boolean.TYPE, Boolean.class);
        tmpClass.put(Byte.TYPE, Byte.class);
        tmpClass.put(Character.TYPE, Character.class);
        tmpClass.put(Short.TYPE, Short.class);
        tmpClass.put(Integer.TYPE, Integer.class);
        tmpClass.put(Long.TYPE, Long.class);
        tmpClass.put(Double.TYPE, Double.class);
        tmpClass.put(Float.TYPE, Float.class);
        tmpClass.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPER = CollectionUtils.unmodifiableMap(tmpClass);
        //
        try {
            tmpClass = CollectionUtils.flip(PRIMITIVE_WRAPPER);
        } catch (ValidateException e) {
            // 忽略空值检查
        }
        WRAPPER_PRIMITIVE = CollectionUtils.unmodifiableMap(tmpClass);
        //
        Map<String, Character> tmpAbbreviate = new HashMap<>(9);
        tmpAbbreviate.put("int", 'I');
        tmpAbbreviate.put("boolean", 'Z');
        tmpAbbreviate.put("float", 'F');
        tmpAbbreviate.put("long", 'J');
        tmpAbbreviate.put("short", 'S');
        tmpAbbreviate.put("byte", 'B');
        tmpAbbreviate.put("double", 'D');
        tmpAbbreviate.put("char", 'C');
        tmpAbbreviate.put("void", 'V');
        ABBREVIATION = CollectionUtils.unmodifiableMap(tmpAbbreviate);
    }

    /**
     * 根据代码获取包名称
     *
     * @param code 代码
     * @return 包名称
     *
     * @throws ValidateException 正则解析器空异常
     */
    public static String getPackageName(String code) throws ValidateException {
        return StringUtils.match(ExpressionEnum.getPattern(ExpressionEnum.PACKAGE_NAME), code, 1);
    }

    /**
     * 根据代码获取类名称
     * <li/>类名称为顺序查找的第一个类
     *
     * @param code 代码
     * @return 类名称
     *
     * @throws ValidateException 正则解析器空异常
     */
    public static String getClassName(String code) throws ValidateException {
        return StringUtils.match(ExpressionEnum.getPattern(ExpressionEnum.CLASS_NAME), code, 1);
    }

    /**
     * 类实例化
     *
     * @param classpath 类路径
     * @param type 返回类型
     * @return 类实例
     *
     * @throws UtilityException 类加载异常
     * @throws ValidateException 验证空异常
     */
    public static <T> T newClass(String classpath, Class<T> type) throws ValidateException, UtilityException {
        return ClassUtils.newClass(classpath, null, type);
    }

    /**
     * 类实例化
     *
     * @param classpath 类路径
     * @param loader 类加载器
     * @param type 返回类型
     * @return 类实例
     *
     * @throws UtilityException 类加载异常
     * @throws ValidateException classpath空异常
     */
    public static <T> T newClass(String classpath, ClassLoader loader, Class<T> type) throws ValidateException, UtilityException {
        Class<?> clazz = ClassUtils.loadClass(classpath, loader, true);
        return ClassUtils.newAndCast(clazz, type);
    }

    /**
     * 类加载
     *
     * @param classpath 类路径
     * @param loader 类加载器
     * @param initialize 是否初始化类
     * @return 类
     *
     * @throws UtilityException 类加载异常
     * @throws ValidateException classpath空异常
     */
    public static Class<?> loadClass(String classpath, ClassLoader loader, boolean initialize) throws ValidateException, UtilityException {
        ValidateUtils.notEmpty("loadClass", classpath);

        if (loader == null) {
            loader = ClassUtils.getClassLoader(PizContext.class, Thread.currentThread());
        }
        try {
            if (ABBREVIATION.containsKey(classpath)) {
                return Class.forName("[" + ABBREVIATION.get(classpath), initialize, loader).getComponentType();
            }
            return Class.forName(classpath, initialize, loader);
        } catch (ClassNotFoundException e) {//
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.FOUND", classpath);
            throw new UtilityException(BasicCodeEnum.MSG_0009, msg, e);
        }
    }

    /**
     * 获取类加载器
     *
     * @return 获取当前类加载器
     */
    public static ClassLoader getClassLoader() {
        return ClassUtils.getClassLoader(null, Thread.currentThread());
    }

    /**
     * 获取类加载器
     *
     * @param target 类
     * @param current 类加载器所在的线程(可选参数)
     * @return 根据条件获取的类加载器
     */
    public static ClassLoader getClassLoader(Class<?> target, Thread current) {
        ClassLoader loader = null;

        if (current != null) {
            try {
                loader = current.getContextClassLoader();
            } catch (SecurityException e) {
                // do nothing
            }
        }
        if (loader == null && target != null) {
            loader = target.getClassLoader();
        }
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        if (current != null) {
            current.setContextClassLoader(loader);
        }
        return loader;
    }

    /**
     * 识别对象返回对象类型
     *
     * @param primitive 是否识别包装类,将之转换为基本类
     * @param arguments 需要识别的对象
     * @return 识别后的对象类型
     */
    public static Class<?>[] toClass(boolean primitive, Object... arguments) {
        if (ArrayUtils.isEmpty(arguments)) {
            return ArrayUtils.EMPTY_CLASS;
        }
        Class<?>[] classTypes = new Class<?>[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                classTypes[i] = null;
            } else if (primitive) {
                try {
                    classTypes[i] = ClassUtils.toPrimitiveClass(arguments[i]);
                } catch (ValidateException e) {
                    classTypes[i] = null;
                }
            } else {
                classTypes[i] = arguments[i].getClass();
            }
        }
        return classTypes;
    }

    /**
     * 识别包装类，将包装类转换为基础类型
     *
     * @param target 识别对象
     * @return 基础类型
     *
     * @throws ValidateException 对象为空异常
     */
    public static Class<?> toPrimitiveClass(Object target) throws ValidateException {
        ValidateUtils.notNull("toPrimitiveClass", target);
        Class<?> tmp = target.getClass();

        if (WRAPPER_PRIMITIVE.containsKey(tmp)) {
            tmp = WRAPPER_PRIMITIVE.get(tmp);
        }
        return tmp;
    }

    /**
     * 识别基本类，将基本类转换为包装类型
     *
     * @param target 识别对象
     * @return 包装类型
     *
     * @throws ValidateException 对象为空异常
     */
    public static Class<?> toWrapperClass(Object target) throws ValidateException {
        ValidateUtils.notNull("toWrapperClass", target);
        Class<?> tmp = target.getClass();

        if (tmp.isPrimitive()) {
            tmp = PRIMITIVE_WRAPPER.get(tmp);
        }
        return tmp;
    }

    public static Class<?>[] getInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return ArrayUtils.EMPTY_CLASS;
        }
        HashSet<Class<?>> tmp = new HashSet<>();
        try {
            ClassUtils.getInterfaces(clazz, tmp);
        } catch (ValidateException e) {
            // 忽略空值异常
        }
        return tmp.toArray(ArrayUtils.EMPTY_CLASS);
    }

    /**
     * 获取类的接口列表
     *
     * @param clazz 类
     * @param cache 用于缓存接口对象
     * @throws ValidateException 验证空异常
     */
    public static void getInterfaces(Class<?> clazz, HashSet<Class<?>> cache) throws ValidateException {
        ValidateUtils.notNull("getInterfaces", 0, cache);

        while (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();

            for (Class<?> item : interfaces) {
                if (cache.add(item)) {
                    ClassUtils.getInterfaces(item, cache);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public static <T> T cast(Object target, Class<T> type) throws ValidateException {
        ValidateUtils.notNull("cast", target, type);
        try {
            return type.cast(target);
        } catch (ClassCastException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.CAST", target.getClass().getName(), type.getName());
            throw new ValidateException(BasicCodeEnum.MSG_0004, msg, e);
        }
    }

    public static <T> T newAndCast(Class<?> target, Class<T> type) throws ValidateException, UtilityException {
        ValidateUtils.notNull("newAndCast", target, type);
        try {
            return type.cast(target.newInstance());
        } catch (ClassCastException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.CAST", target.getName(), type.getName());
            throw new UtilityException(BasicCodeEnum.MSG_0004, msg, e);
        } catch (InstantiationException | IllegalAccessException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.INIT", target.getName(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0001, msg, e);
        }
    }

    /**
     * 简单填充
     *
     * @param target 目标对象
     * @param properties 填充参数
     * @throws ValidateException target参数验证异常
     */
    public static void simplePopulate(Object target, TupleObject properties) throws ValidateException {
        ValidateUtils.notNull("simplePopulate", target);

        if (properties != null) {
            for (Map.Entry<String, Object> item : properties.entrySet()) {
                try {
                    ReflectUtils.invokeSetField(item.getKey(), target, item.getValue(), true);
                } catch (BaseException e) {
                    // do nothing
                }
            }
        }
    }
}

package org.pizazz2.common;

import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.Locale;

/**
 * Lambda表达式反射处理
 *
 * @author xlgp2171
 * @version 2.0.210525
 */
public class LambdaUtils {
    /**
     * 序列化为Lambda对象详情
     *
     * @param sFunction 实现Serializable接口的lambda对象
     * @return Lambda对象详情
     */
    public static SerializedLambda resolve(Serializable sFunction) throws ValidateException, UtilityException {
        ValidateUtils.isSyntheticClass("resolve", sFunction);
        // 获取默认的序列化方法返回的映射
        return ReflectUtils.invokeMethod(sFunction, "writeReplace", ArrayUtils.EMPTY_CLASS,
                ArrayUtils.EMPTY_OBJECT, SerializedLambda.class, true);
    }

    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }
        // 若没有is/get/set则不作修改
        // 将第一个字符小写
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static String toColumnName(Serializable sFunction) throws ValidateException, UtilityException {
        SerializedLambda lambda = LambdaUtils.resolve(sFunction);
        return LambdaUtils.methodToProperty(lambda.getImplMethodName());
    }
}

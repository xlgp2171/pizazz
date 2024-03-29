package org.pizazz2.common;

import org.pizazz2.common.ref.IFieldSerializable;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

/**
 * Lambda表达式反射处理
 *
 * @author xlgp2171
 * @version 2.1.211028
 */
public class LambdaUtils {
    static final String PREFIX_IS = "is";
    static final String PREFIX_GET = "get";
    static final String PREFIX_SET = "set";
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
        String methodName = name.toLowerCase();

        if (methodName.startsWith(LambdaUtils.PREFIX_IS)) {
            name = name.substring(LambdaUtils.PREFIX_IS.length());
        } else if (methodName.startsWith(LambdaUtils.PREFIX_GET) || methodName.startsWith(LambdaUtils.PREFIX_SET)) {
            name = name.substring(LambdaUtils.PREFIX_GET.length());
        }
        // 若没有is/get/set则不作修改
        // 将第一个字符小写
        if (name.length() == NumberUtils.ONE.intValue() || (name.length() > 1 &&
                !Character.isUpperCase(name.charAt(1)))) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static <T> String toFieldName(IFieldSerializable<T> sFunction) throws ValidateException, UtilityException {
        SerializedLambda lambda = LambdaUtils.resolve(sFunction);
        return LambdaUtils.methodToProperty(lambda.getImplMethodName());
    }
}

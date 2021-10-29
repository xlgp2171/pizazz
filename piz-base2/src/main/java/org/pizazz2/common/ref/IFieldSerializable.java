package org.pizazz2.common.ref;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Lambda表达式字段处理接口<br>
 * <pre>
 * IFieldSerializable<Point, ?> field = Point::getX;
 * </pre>
 *
 * @author xlgp2171
 * @version 2.1.211028
 *
 * @param <T> 实体类型
 */
@FunctionalInterface
public interface IFieldSerializable<T> extends Function<T, Object>, Serializable {
}

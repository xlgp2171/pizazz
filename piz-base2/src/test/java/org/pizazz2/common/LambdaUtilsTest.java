package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.exception.UtilityException;

import java.io.Serializable;
import java.util.function.Function;

/**
 * LambdaUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210525
 */
public class LambdaUtilsTest {

    @Test
    public void testToColumnName() throws UtilityException {
        SFunction<LinkedObject<String>, ?> sFunction = LinkedObject::getChildren;
        String column = LambdaUtils.toColumnName(sFunction);
        Assert.assertEquals(column, "children");
    }

    @FunctionalInterface
    private interface SFunction<T, R> extends Function<T, R>, Serializable {
    }
}

package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.test.ParentObject;
import org.pizazz2.test.SFunction;

/**
 * LambdaUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210525
 */
public class LambdaUtilsTest {

    @Test
    public void testToColumnName() throws UtilityException {
        SFunction<ParentObject, ?> sFunction = ParentObject::getParentName;
        String column = LambdaUtils.toColumnName(sFunction);
        Assert.assertEquals(column, "parentName");
    }
}

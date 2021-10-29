package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.test.ParentObject;

/**
 * LambdaUtils测试
 *
 * @author xlgp2171
 * @version 2.0.211028
 */
public class LambdaUtilsTest {

    @Test
    public void testToFieldName() throws UtilityException {
        String column = LambdaUtils.toFieldName(ParentObject::getParentName);
        Assert.assertEquals(column, "parentName");
    }
}

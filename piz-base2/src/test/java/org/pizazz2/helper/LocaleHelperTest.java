package org.pizazz2.helper;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.message.TypeEnum;

import java.util.Locale;

/**
 * ConfigureHelper测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class LocaleHelperTest {

    @BeforeClass
    public static void setUp() {
        LocaleHelper.validate(TypeEnum.BASIC, Locale.CHINESE);
    }

    @Test
    public void testToLocaleText() {
        String result = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "A", "B");
        Assert.assertEquals(result, "目标 A 的第 B 个输入参数为空值");
    }
}

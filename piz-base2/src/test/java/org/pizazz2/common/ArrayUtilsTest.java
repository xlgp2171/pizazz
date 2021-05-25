package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ArrayUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ArrayUtilsTest {
    @Test
    public void testIsEmpty() {
        Object[] target = new Object[] {};
        boolean result = ArrayUtils.isEmpty(target);
        Assert.assertTrue(result);
    }

    @Test
    public void testInsert() {
        Object[] target = new Object[] { "A", "B", "C" };
        int index = 2;
        String element = "X";
        Object[] result = ArrayUtils.insert(target, index, element);
        Assert.assertArrayEquals(result, new String[] { "A", "B", "X", "C" });
    }

    @Test
    public void testContains() {
        Object[] target = new Object[] { "A", "B", "2", "C" };
        String element = "2";
        boolean result = ArrayUtils.contains(target, element);
        Assert.assertTrue(result);
    }

    @Test
    public void testNullToEmpty() {
        Object[] target = null;
        target = ArrayUtils.nullToEmpty(target);
        Assert.assertArrayEquals(target, new Object[] {});
    }

    @Test
    public void testNewArray() {
        String[] result = ArrayUtils.newArray(3, "E");
        Assert.assertArrayEquals(result, new String[] { "E", "E", "E" });
    }

    @Test
    public void testMerge() {
        String[] right = new String[] { "1", "2" };
        String[] left = new String[] { "A", "B", "C" };
        String[] result = ArrayUtils.merge(left, right);
        Assert.assertArrayEquals(result, new String[] { "A", "B", "C", "1", "2" });
    }

    @Test
    public void testAsList() {
        List<String> list = ArrayUtils.asList("A", "B");
        Assert.assertEquals(list.size(), 2);
        Assert.assertEquals(list.getClass(), ArrayList.class);
    }

    @Test
    public void testAsSet() {
        Set<String> set = ArrayUtils.asSet("A", "B", "A");
        Assert.assertEquals(set.size(), 2);
        Assert.assertEquals(set.getClass(), HashSet.class);
    }
}

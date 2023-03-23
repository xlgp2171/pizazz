package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * CollectionUtils测试
 *
 * @author xlgp2171
 * @version 2.2.230323
 */
public class CollectionUtilsTest {
    @Test
    public void testMerge() {
        Set<String> target = new HashSet<>();
        target.add("A");
        target.add("B");
        target.add("X");
        CollectionUtils.merge(target, new String[]{"C", "D", "X"});
        Assert.assertEquals(target.size(), 5);
    }

    @Test
    public void testFlip() {
        Map<String, Integer> target = new HashMap<>();
        target.put("A", 1);
        target.put("B", 2);
        target.put("C", 3);
        target.put("D", 1);
        Map<Integer, String> result = CollectionUtils.flip(target);
        Assert.assertEquals(result.get(1), "D");
    }

    @Test
    public void testConvert() {
        List<Object> target = new LinkedList<>();
        target.add(true);
        target.add(1);
        List<String> result = CollectionUtils.convert(target);
        Assert.assertEquals(result.get(0), "true");
    }

    @Test
    public void testToList1() {
        String[] target = new String[]{ "1", "2", "3" };
        List<Long> result = CollectionUtils.toList(target, Long.class);
        Assert.assertEquals(result.get(1).longValue(), 2);
    }

    @Test
    public void testToList2() {
        Object target = new Date();
        List<Date> result = CollectionUtils.toList(target, Date.class);
        Assert.assertEquals(result.get(0), target);
    }
}

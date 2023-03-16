package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.DateUtils;
import org.pizazz2.tool.ref.IdObject;

import java.util.*;
import java.util.concurrent.*;

/**
 * IdFactory测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class IdFactoryTest {
    @Test
    public void testParse() {
        IdObject object = IdFactory.parseObject(730114058750328832L);
        System.out.println(object);
        String format = DateUtils.format(object.getTimestamp(), DateUtils.DEFAULT_FORMAT);
        System.out.println(format);
    }


    @Test
    public void testGenerateAndParseObject() {
        short custom = 16;
        long id1 = IdFactory.newInstance(custom).generate();
        IdBuilder builder = IdFactory.newInstance();
        long id2 = builder.generate();
        short c1 = IdFactory.parseObject(id1).getCustom();
        Assert.assertEquals(c1, custom);
        short c2 = IdFactory.parseObject(id2).getCustom();
        Assert.assertEquals(c2, 0);
    }

    @Test
    public void testGenerateByMultiThread() throws InterruptedException, ExecutionException {
        int target = 64;
        IdBuilder builder = IdFactory.newInstance();
        ExecutorService pool = Executors.newFixedThreadPool(target);
        CompletionService<Long> service = new ExecutorCompletionService<>(pool);

        for (int i = 0; i < target; i++) {
            service.submit(builder::generate);
        }
        pool.shutdown();
        Set<Long> tmp = new HashSet<>();
        Future<Long> result = service.poll();

        do {
            tmp.add(result.get());
            result = service.poll();
        } while (result != null);
        Assert.assertEquals(tmp.size(), target);
    }
}

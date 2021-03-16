package org.pizazz2.tool;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.common.PathUtils;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * MemoryMapping测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class MemoryMappingTest {

    static Path PATH;

    @BeforeClass
    public static void setUp() throws UtilityException {
        PATH = PathUtils.copyToTemp("".getBytes(), "piz_mapping_");
    }

    @Test
    public void testSentMessageAndReceiveMessage() throws ToolException {
        String target = "中文";
        MemoryMapping mapping = new MemoryMapping(PATH, 0, 128);
        long sessionId = 1L;
        long a = System.currentTimeMillis();
        boolean success = mapping.sentMessage(lock -> target.getBytes(StandardCharsets.UTF_8), sessionId);
        Assert.assertTrue(success);
        byte[] result = mapping.receiveMessage(item -> item == sessionId);
        Assert.assertEquals(new String(result, StandardCharsets.UTF_8), target);
    }

    public void sentMessageExample() throws ToolException {
        MemoryMapping mapping = new MemoryMapping(PATH, 0, 128);
        long sessionId = 12345L;
        long a = System.currentTimeMillis();

        for (int i = 0; i < 1_000; i ++) {
            final String num = String.valueOf(i);
            boolean success;
            do {
                success = mapping.sentMessage(lock -> "S:".concat(num).getBytes(), sessionId);
            } while (!success);

            while (true) {
                byte[] result = mapping.receiveMessage(item -> item != sessionId);

                if (result != null) {
                    break;
                }
            }
        }
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }

    public void receiveMessageExample() throws ToolException, InterruptedException {
        MemoryMapping mapping = new MemoryMapping(PATH, 0, 128);
        long sessionId = 54321L;

        while (true) {
            byte[] result = mapping.receiveMessage(item -> item != sessionId);

            if (result == null) {
                continue;
            }
            String info = new String(result).trim();
            System.out.println(info);
            boolean success;
            do {
                success = mapping.sentMessage(lock -> "R:".concat(info.substring(2)).getBytes(), sessionId);
            } while (!success);
        }
    }

    @AfterClass
    public static void tearDown() throws UtilityException {
        PathUtils.delete(PATH, false);
    }
}
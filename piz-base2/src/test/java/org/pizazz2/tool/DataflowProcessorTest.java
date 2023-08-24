package org.pizazz2.tool;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.DateUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.tool.ref.IData;
import org.pizazz2.tool.ref.IDataflowListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * DataflowProcessor测试
 *
 * @author xlgp2171
 * @version 2.0.210827
 */
public class DataflowProcessorTest {

    static DataflowProcessor<ContentObject> PROCESSOR;
    static final int PROCESS_TIME = 1000;
    static final boolean SYNC = false;
    static final int ACTIONS = 4;
    static final long SIZE = 8;
    static final long INTERVAL = 1000;
    static final int THREADS = 2;

    static final IDataflowListener<ContentObject> LISTENER = (executionId, data) -> {};

    @BeforeClass
    public static void init() {
        PROCESSOR = DataflowProcessor.builder((dataList, config) -> {
            String datetime = DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSS", null);
            String tmp = dataList.stream().map(ContentObject::getName).collect(Collectors.joining(","));
            System.out.println(datetime + ",size=" + tmp);
            try {
                Thread.sleep(PROCESS_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, LISTENER).setActions(ACTIONS).setSize(SIZE).setInterval(INTERVAL).setSync(SYNC).setThreads(THREADS).build();
    }

    @Test
    public void testActions() throws InterruptedException {
        for (int i = 0; i < 9; i++) {
            PROCESSOR.add(new ContentObject("A" + i, ArrayUtils.EMPTY_BYTE));
        }
        System.out.println("finish");
        Thread.sleep(10000);
    }

    @Test
    public void testSize() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            PROCESSOR.add(new ContentObject("B" + i, "XXX".getBytes()));
        }
        Thread.sleep(1000);
    }

    @Test
    public void testInterval() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            PROCESSOR.add(new ContentObject("C" + i, ArrayUtils.EMPTY_BYTE));
            Thread.sleep(700);
        }
        Thread.sleep(1000);
    }

    @Test
    public void testBigData() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            PROCESSOR.add(new ContentObject("D" + i, "XXX".getBytes()));
        }
        System.out.println("FINISHED");
        Thread.sleep(10000);
    }

    @AfterClass
    public static void destroy() {
        SystemUtils.destroy(PROCESSOR, null);
    }

    static class ContentObject implements IData {

        private Charset charset = StandardCharsets.UTF_8;
        private final String name;
        private final byte[] data;

        public ContentObject(String name, byte[] data) throws ValidateException {
            this.name = name;
            this.data = data;
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return new String(data, charset);
        }

        @Override
        public int length() {
            return data.length;
        }
    }
}

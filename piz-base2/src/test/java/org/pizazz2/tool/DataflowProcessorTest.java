package org.pizazz2.tool;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.DateUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.tool.ref.IDataflowListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataflowProcessor测试
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class DataflowProcessorTest {

    static DataflowProcessor<ContentObject> PROCESSOR;
    static final boolean SYNC = false;
    static final int ACTIONS = 4;
    static final long SIZE = 8;
    static final long INTERVAL = 1000;
    static final int THREADS = 2;

    static final IDataflowListener<ContentObject> LISTENER = new IDataflowListener<ContentObject>() {
        @Override
        public void before(long executionId, List<ContentObject> data) {
            System.out.println("BEFORE,id=" + executionId + ",data=" + data.size());
        }

        @Override
        public void after(long executionId, List<ContentObject> data) {
            System.out.println("AFTER,id=" + executionId + ",data=" + data.size());
        }

        @Override
        public void exception(long executionId, List<ContentObject> data, Exception e) {
            System.err.println("ERROR:" + e.getMessage() + ",id=" + executionId + ",data=" + data.size());
        }
    };

    @BeforeClass
    public static void init() {
        PROCESSOR = DataflowProcessor.builder((dataList, config) -> {
            String datetime = DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSS", null);
            String tmp = dataList.stream().map(ContentObject::getName).collect(Collectors.joining(","));
            System.out.println(datetime + ",size=" + tmp);
        }, LISTENER).setActions(ACTIONS).setSize(SIZE).setInterval(INTERVAL).setSync(SYNC).setThreads(THREADS).build();
    }

    @Test
    public void testActions() throws InterruptedException {
        for (int i = 0; i < 9; i++) {
            PROCESSOR.add(new ContentObject(-1, "A" + i, ArrayUtils.EMPTY_BYTE));
        }
        Thread.sleep(1000);
    }

    @Test
    public void testSize() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            PROCESSOR.add(new ContentObject(-1, "B" + i, "XXX".getBytes()));
        }
        Thread.sleep(1000);
    }

    @Test
    public void testInterval() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            PROCESSOR.add(new ContentObject(-1, "C" + i, ArrayUtils.EMPTY_BYTE));
            Thread.sleep(700);
        }
        Thread.sleep(1000);
    }

    @AfterClass
    public static void destroy() {
        SystemUtils.destroy(PROCESSOR, null);
    }

    static class ContentObject extends LinkedObject<byte[]> {

        private Charset charset = StandardCharsets.UTF_8;

        public ContentObject(long id, String name, byte[] data) throws ValidateException {
            super(id, name, StringUtils.EMPTY, data);
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }

        public String getContent() {
            return new String(getData(), charset);
        }
    }
}

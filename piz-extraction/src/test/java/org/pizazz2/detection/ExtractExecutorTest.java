package org.pizazz2.detection;

import org.apache.tika.metadata.Metadata;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.PizContext;
import org.pizazz2.common.DateUtils;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.TextExtraction;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ExtractProcessor;
import org.pizazz2.extraction.process.ConcurrentProcessor;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.TikaHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.tool.DataflowProcessor;
import org.pizazz2.tool.ref.IDataflowListener;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class ExtractExecutorTest {
//    static final String NAME = "sample_attachment.eml";
//    static final String NAME = "sample_attachment.msg";
//    static final String NAME = "sample_charset.eml";
//    static final String NAME = "sample_charset.msg";
//    static final String NAME = "sample_old.hwp";
//    static final String NAME = "sample_v5.hwp";
    static final String NAME = "sample.zip";
//    static final String NAME = "sample.gz";
//    static final String NAME = "sample_v5.rar";
//    static final String NAME = "sample_old.rar";
//    static final String NAME = "sample.tar.gz";
//    static final String NAME = "sample.7z";

    static final String DIR = "src/test/resources";

    static final boolean OUTPUT = false;

    @BeforeClass
    public static void setUp() {
        System.setProperty(PizContext.NAMING_SHORT + ".rar.path", "D:/Tools/WinRAR/Rar.exe");
    }

    @Test
    public void testExtract() throws BaseException {
        TextExtraction executor = new TextExtraction(-1);
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), NAME, DIR + "/").setData(data);

        long a = System.currentTimeMillis();
        extract(executor, object, TupleObjectHelper.emptyObject());
        println(object, OUTPUT, 0);
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testExtractFromDir() throws BaseException {
        TextExtraction executor = new TextExtraction(-1);
        TupleObject config = TupleObjectHelper.emptyObject();
        Path[] paths = PathUtils.listPaths(Paths.get(DIR), null, false);
        List<ExtractObject> tmp = new LinkedList<>();

        for (Path item : paths) {
            tmp.add(new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(), DIR + "/")
                    .setData(PathUtils.toByteArray(item)));
        }
        long a = System.currentTimeMillis();

        for (ExtractObject object : tmp) {
            extract(executor, object, config);
            println(object, OUTPUT, 0);
            System.out.println("---------- ---------- " + object.getId() + " ---------- ----------");
        }
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testConcurrent() throws BaseException {
        ConcurrentProcessor processor = new ConcurrentProcessor(new ExtractProcessor(), 4);
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), NAME, DIR + "/").setData(data);

        long a = System.currentTimeMillis();
        processor.execute(object, TupleObjectHelper.emptyObject());
        println(object, OUTPUT, 0);
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testConcurrentFromDir() throws BaseException {
        TupleObject config = TupleObjectHelper.emptyObject();
        Path[] paths = PathUtils.listPaths(Paths.get(DIR), null, false);
        ConcurrentProcessor processor = new ConcurrentProcessor(new ExtractProcessor(), 4);
        List<ExtractObject> tmp = new LinkedList<>();

        for (Path item : paths) {
            tmp.add(new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(), DIR + "/")
                    .setData(PathUtils.toByteArray(item)));
        }
        long a = System.currentTimeMillis();
        processor.executeBatch(tmp,config);

        for (ExtractObject item : tmp) {
            println(item,OUTPUT,0);
            System.out.println("---------- ---------- " + item.getId() + " ---------- ----------");
        }
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testDataflow() throws UtilityException, InterruptedException {
        final IDataflowListener<ExtractObject> listener = (executionId, dataList) -> {
            for (ExtractObject item : dataList) {
                println(item,OUTPUT,0);
            }
            System.out.println("---------- ---------- " + executionId + " ---------- ----------");
        };
        // 并行处理器
        ConcurrentProcessor concurrent = new ConcurrentProcessor(new ExtractProcessor(), 4);
        // 流式处理器
        DataflowProcessor<ExtractObject> dataflow = DataflowProcessor.builder(concurrent::executeBatch, listener)
                .setActions(4).setSize(1024 * 1024).setInterval(3000).setSync(false).setThreads(1).build();

        Path[] paths = PathUtils.listPaths(Paths.get(DIR), null, false);

        for (Path item : paths) {
            ExtractObject object = new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(),
                    DIR + "/").setData(PathUtils.toByteArray(item));
            dataflow.add(object);
        }
        dataflow.destroy(Duration.ofMillis(10000));
        concurrent.destroy(null);
    }

    @Test
    public void testExtractByTika() throws ParseException, UtilityException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        Metadata metadata = new Metadata();
        String content = TikaHelper.extractToText(data, metadata, StandardCharsets.UTF_8);
        System.out.println("META: " + metadata);
        System.out.println("TEXT: " + content);
    }

    void extract(TextExtraction executor, String name, byte[] data, String source, TupleObject config)
            throws DetectionException, ParseException {
        ExtractObject object = executor.extract(name, source, data, config);
        extract(executor, object, config);
    }

    void extract(TextExtraction executor, ExtractObject object, TupleObject config)
            throws DetectionException, ParseException {
        executor.extract(object, config);

        if (object.hasAttachment()) {
            for (ExtractObject item : object.getAttachment()) {
                extract(executor, item, config);
            }
        }
    }

    void println(ExtractObject object, boolean output, int level) {
        System.out.println(multiSpace(level) + "[" + object.getStatus().name() + " / " + object.getType() + "]: " +
                object + "(" + object.getSource() + ")");

        if (output) {
            String text = object.getMetadata().toString();
            System.out.println(multiSpace(level) + "META: " + (text.length() > 40 ? text.substring(0, 40) + " ..." :
                    text));
            text = object.getContent();
            System.out.println(multiSpace(level) + "TEXT:" + (text == null ? "None" : (text.length() > 40 ?
                    text.substring(0, 40) + " ..." : text)));
        }
        if (object.hasAttachment()) {
            for (ExtractObject item : object.getAttachment()) {
                println(item, output, level + 1);
            }
        }
    }

    String multiSpace(int level) {
        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < level; i ++) {
            tmp.append("\t");
        }
        return tmp.toString();
    }


}

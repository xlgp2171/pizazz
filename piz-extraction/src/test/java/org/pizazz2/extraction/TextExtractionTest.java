package org.pizazz2.extraction;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.PizContext;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ExportProcessor;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.tool.DataflowProcessor;
import org.pizazz2.tool.ref.IDataflowListener;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class TextExtractionTest extends TestBase {
//        static final String NAME = "sample_attachment.eml";
//        static final String NAME = "sample_attachment.msg";
    //    static final String NAME = "sample_charset.eml";
    //    static final String NAME = "sample_charset.msg";
    //    static final String NAME = "sample_old.hwp";
    //    static final String NAME = "sample_v5.hwp";
    static final String NAME = "sample.zip";
    //    static final String NAME = "sample.gz";
    //    static final String NAME = "sample_v5.rar";
//        static final String NAME = "sample_old.rar";
    //    static final String NAME = "sample.tar.gz";
    //    static final String NAME = "sample.7z";

    static final String DIR = "src/test/resources";

    static final boolean OUTPUT = false;
    static final int PARALLELISM = 4;

    static final String RAR5_EXE_PATH = "D:/Tools/WinRAR/Rar.exe";

    @BeforeClass
    public static void setUp() {
        System.setProperty(PizContext.NAMING_SHORT + ".rar.path", RAR5_EXE_PATH);
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
        TextExtraction extraction = new TextExtraction(PARALLELISM);
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), NAME, DIR + "/").setData(data);
        long a = System.currentTimeMillis();
        extraction.getConcurrent().execute(object, TupleObjectHelper.emptyObject());
        println(object, OUTPUT, 0);
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testConcurrentFromDir() throws BaseException {
        Path[] paths = PathUtils.listPaths(Paths.get(DIR), null, false);
        List<ExtractObject> tmp = new LinkedList<>();

        for (Path item : paths) {
            tmp.add(new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(), DIR + "/")
                    .setData(PathUtils.toByteArray(item)));
        }
        TextExtraction extraction = new TextExtraction(PARALLELISM);
        long a = System.currentTimeMillis();
        extraction.extract(tmp, TupleObjectHelper.emptyObject());

        for (ExtractObject item : tmp) {
            println(item, OUTPUT, 0);
            System.out.println("---------- ---------- " + item.getId() + " ---------- ----------");
        }
        System.out.println("COST: " + (System.currentTimeMillis() - a));
    }

    @Test
    public void testDataflow() throws UtilityException {
        final IDataflowListener<ExtractObject> listener = (executionId, dataList) -> {
            for (ExtractObject item : dataList) {
                println(item,OUTPUT,0);
            }
            System.out.println("---------- ---------- " + executionId + " ---------- ----------");
        };
        TextExtraction extraction = new TextExtraction(PARALLELISM);
        // 流式处理器
        DataflowProcessor<ExtractObject> dataflow = DataflowProcessor.builder(extraction::extract, listener)
                .setActions(4).setSize(1024 * 1024).setInterval(3000).setSync(false).setThreads(1).build();

        Path[] paths = PathUtils.listPaths(Paths.get(DIR), null, false);

        for (Path item : paths) {
            ExtractObject object = new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(),
                    DIR + "/").setData(PathUtils.toByteArray(item));
            dataflow.add(object);
        }
        dataflow.destroy(Duration.ofMillis(10000));
        extraction.destroy(null);
    }

    @Test
    public void testExport() throws DetectionException, ParseException, UtilityException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        TextExtraction extraction = new TextExtraction();
        ExtractObject object = extraction.extract(NAME, "X:/extract_test/", data,
                TupleObjectHelper.emptyObject(), false);
        println(object, false, 0);
//        Path base = PathUtils.createTempDirectory("extract_test");
        Path base = Paths.get("E:\\可以删除\\" + NAME );
        System.out.println("EXPORT:" + base);
        new ExportProcessor(base).exportAll(object);
    }
}

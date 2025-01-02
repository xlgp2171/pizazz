package org.pizazz2.extraction;

import org.apache.tika.metadata.Metadata;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pizazz2.PizContext;
import org.pizazz2.common.*;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.*;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ExportProcessor;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.TypeHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.tool.DataflowProcessor;
import org.pizazz2.tool.ref.IDataflowListener;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽取模块测试
 */
public class ExtractionTestCase extends TestBase {
    // 抽取文件名称
    static final String NAME = "sample.zip";
    // 抽取资源文件夹
    static final String DIR_PATH = "src/test/resources/";
    // 是否输出正文内容
    static final boolean PRINT_CONTENT = true;
    // 抽取线程数
    static final int PARALLELISM = 6;
    // rar文件路径，主要用于解压rar v5
    static final String RAR5_EXE_PATH = "D:/CommonTool/WinRAR_6.21/Rar.exe";

    @BeforeClass
    public static void setUp() {
        // 设置环境变量
        System.setProperty(PizContext.NAMING_SHORT + ".rar.path", RAR5_EXE_PATH);
    }

    // 抽取一个文件
    @Test
    public void testExtract() throws BaseException {
        extractFile(new TextExtraction());
    }

    private void extractFile(TextExtraction executor) throws UtilityException, DetectionException, ParseException {
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), NAME, DIR_PATH)
                .setData(IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME)));
        // 抽取计时
        long x = System.currentTimeMillis();
        extract(executor, object, TupleObjectHelper.emptyObject());
        println(object, PRINT_CONTENT, 0);
        System.out.println("\nCOST:\t" + (System.currentTimeMillis() - x));
        executor.close();
    }

    // 抽取一个文件夹内所有文件
    @Test
    public void testExtractDirectory() throws BaseException {
        extractDirectory(new TextExtraction());
    }

    private void extractDirectory(TextExtraction executor) throws UtilityException, DetectionException, ParseException {
        TupleObject config = TupleObjectHelper.emptyObject();
        // 抽取文件夹
        List<ExtractObject> objects = Arrays.stream(
                PathUtils.listPaths(Paths.get(DIR_PATH), null, false)).map(path -> {
            ExtractObject object = new ExtractObject(
                    ExtractHelper.generateId(), path.getFileName().toString(), DIR_PATH);
            try {
                object.setData(PathUtils.toByteArray(path));
            } catch (UtilityException e) {
                System.err.println(e.getMessage());
            }
            return object;
        }).filter(item -> item.length() > 0).collect(Collectors.toList());
        long x = System.currentTimeMillis();

        if (executor.getConcurrent() != null) {
            executor.getConcurrent().executeBatch(objects, config);

            objects.forEach(item -> {
                System.out.println("---------- ---------- " + item.getId() + " ---------- ----------");
                println(item, PRINT_CONTENT, 0);
            });
        } else {
            objects.forEach(item -> {
                System.out.println("---------- ---------- " + item.getId() + " ---------- ----------");
                try {
                    extract(executor, item, config);
                } catch (DetectionException | ParseException e) {
                    System.err.println(e.getMessage());
                }
                println(item, PRINT_CONTENT, 0);
            });
        }
        System.out.println("\nCOST:\t" + (System.currentTimeMillis() - x));
        executor.close();
    }

    // 多线程抽取文件
    @Test
    public void testConcurrent() throws BaseException {
        extractFile(new TextExtraction(PARALLELISM));
    }

    // 多线程抽取文件夹
    @Test
    public void testConcurrentDirectory() throws BaseException {
        extractDirectory(new TextExtraction(PARALLELISM));
    }

    // 流式处理抽取文件夹
    @Test
    public void testDataflowDirectory() throws UtilityException {
        final IDataflowListener<ExtractObject> listener = (executionId, dataList) -> {
            System.out.println("---------- ---------- " + executionId + " ---------- ----------");

            for (ExtractObject item : dataList) {
                println(item,PRINT_CONTENT,0);
            }
        };
        TextExtraction extraction = new TextExtraction(PARALLELISM);
        // 流式处理器
        DataflowProcessor<ExtractObject> dataflow = DataflowProcessor.builder(extraction::extract, listener)
                .setActions(4).setSize(1024 * 1024 * 5).setInterval(3000).setSync(false).setThreads(1).build();
        Path[] paths = PathUtils.listPaths(Paths.get(DIR_PATH), null, false);
        long x = System.currentTimeMillis();
        for (Path item : paths) {
            dataflow.add(new ExtractObject(ExtractHelper.generateId(), item.getFileName().toString(), DIR_PATH)
                    .setData(PathUtils.toByteArray(item)));
        }
        dataflow.destroy(Duration.ofMillis(20000));
        System.out.println("\nCOST:\t" + (System.currentTimeMillis() - x));
        extraction.close();
    }

    // 文件导出测试(可解压压缩文件)
    @Test
    public void testExport() throws DetectionException, ParseException, UtilityException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        TextExtraction extraction = new TextExtraction();
        ExtractObject object = extraction.extract(NAME, "X:/extract_test/", data,
                TupleObjectHelper.emptyObject(), true);
        println(object, false, 0);
//        Path base = PathUtils.createTempDirectory("extract_test");
        Path base = Paths.get("D:\\Temporary\\" + NAME );
        System.out.println("EXPORT:" + base);
        new ExportProcessor(base).exportAll(object);
    }

    @Test
    public void testMultiFeature() throws UtilityException, DetectionException, ParseException {
        Path path = Paths.get(DIR_PATH + NAME);
        byte[] data = PathUtils.toByteArray(path);
        String name = path.getFileName().toString();
        String directory = path.getParent().toString();
        //
        Set<String> sourceTypeFilter = ArrayUtils.asSet("application/xml", "text/csv");
        TextExtraction extraction = new TextExtraction(-1, new IExtractListener() {
            @Override
            public void detected(ExtractObject target) {
                if (sourceTypeFilter.contains(target.getTypeString())) {
                    ExtractHelper.updateContent(target, ArrayUtils.isEmpty(target.getData()) ? StringUtils.EMPTY :
                            new String(target.getData(), StandardCharsets.UTF_8));
                }
                System.out.println("DETECTED:\t" + target.getSource() + target.getName());
            }

            @Override
            public void parsed(ExtractObject target, IConfig config) {
                System.out.println("PARSED:\t" + target.getSource() + target.getName());
            }

            @Override
            public void extracted(ExtractObject target) {
                System.out.println("EXTRACTED:\t" + target.getSource() + target.getName());
            }

            @Override
            public void exception(ExtractObject target, Exception e) {
                System.err.println(e.getMessage());
            }
        });
        // 黑名单设置
        extraction.getExtractConfig().setTypeBlackList(sourceTypeFilter);
        // 抽取内容格式
        TupleObject config = TupleObjectHelper.newObject("textFormat", "XML");
        // 抽取
        ExtractObject object = extraction.extract(name, directory, data, config, true);
        println(object, PRINT_CONTENT, 0);
        // 提取属性
        FileProperty property = TypeHelper.toProperty(object.getMetadata(), FileProperty.class);
        System.out.println(property.getClass().getSimpleName() + " " + ReflectUtils.invokeGetFields(property, true));

        for (ExtractObject item : object.getAttachment()) {
            property = TypeHelper.toProperty(item.getMetadata(), FileProperty.class);
            System.out.println(property.getClass().getSimpleName() + " " +
                    ReflectUtils.invokeGetFields(property, true));
        }
    }
}

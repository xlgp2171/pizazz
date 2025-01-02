package org.pizazz2.extraction;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;
import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.EncryptionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.TikaHelper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * tika测试类
 */
public class TikaTestCase {
    // 抽取文件名称
    static final String NAME = "message.txt";
    // 抽取资源文件夹
    static final Path DIR = Paths.get("src/test/resources");

    // 抽取文件内容
    @Test
    public void testExtractByTika() throws ParseException, EncryptionException, UtilityException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        Metadata metadata = new Metadata();
        String content = TikaHelper.extractToText(data, metadata, StandardCharsets.UTF_8);
        System.out.println("META:\n" + metadata);
        System.out.println("TEXT:\n" + content);
    }


    // 识别文件夹所有文件类型
    @Test
    public void testDetectAll() throws UtilityException, DetectionException {
        Path[] paths = PathUtils.listPaths(DIR, null, false);

        for (Path item : paths) {
            Metadata metadata = new Metadata();
            MediaType detect = TikaHelper.detect(item, metadata);
            System.out.println(item.getFileName() + ":\t" + detect.toString());
        }
    }

    // 识别文件类型
    @Test
    public void testDetect() throws UtilityException, DetectionException {
        Path path = DIR.resolve(NAME);
        Metadata metadata = new Metadata();
        MediaType detect = TikaHelper.detect(path, metadata);
        System.out.println(detect.toString() + " : " + path.getFileName());
    }

    // 列出所有的支持识别类型
    @Test
    public void testDisplaySupportedTypes() {
        AutoDetectParser parser = new AutoDetectParser();
        MediaTypeRegistry registry = parser.getMediaTypeRegistry();
        Map<MediaType, Parser> parsers = parser.getParsers();

        for (MediaType type : registry.getTypes()) {
            System.out.println(type);

            for (MediaType alias : registry.getAliases(type)) {
                System.out.println("\talias:\t" + alias);
            }
            MediaType supertype = registry.getSupertype(type);

            if (supertype != null) {
                System.out.println("\tsupertype:\t" + supertype);
            }
            Parser p = parsers.get(type);

            if (p != null) {
                if (p instanceof CompositeParser) {
                    p = ((CompositeParser)p).getParsers().get(type);
                }
                System.out.println("\tparser:\t" + p.getClass().getName());
            }
        }
    }

    // 列出所有的探测器
    @Test
    public void testDisplayDetector() {
        displayDetector(TikaConfig.getDefaultConfig().getDetector(), 0);
    }

    private void displayDetector(Detector d, int i) {
        boolean isComposite = (d instanceof CompositeDetector);
        String name = d.getClass().getName();
        System.out.println(indent(i) + name + (isComposite ? " (Composite Detector):" : ""));

        if (isComposite) {
            List<Detector> subDetectors = ((CompositeDetector)d).getDetectors();

            for(Detector sd : subDetectors) {
                displayDetector(sd, i + 2);
            }
        }
    }

    private String indent(int indent) {
        return "                     ".substring(0, indent);
    }
}

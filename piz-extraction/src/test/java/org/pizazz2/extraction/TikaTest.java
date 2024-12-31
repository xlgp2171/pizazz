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

public class TikaTest {
    static final String NAME = "sample.zip";
    static final Path RESOURCE_PATH = Paths.get("src\\test\\resources");

    @Test
    public void testExtractByTika() throws ParseException, EncryptionException, UtilityException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        Metadata metadata = new Metadata();
        String content = TikaHelper.extractToText(data, metadata, StandardCharsets.UTF_8);
        System.out.println("META: " + metadata);
        System.out.println("TEXT: " + content);
    }

    @Test
    public void testMediaType() {
        MediaType type = MediaType.parse("audio/pcm");
        System.out.println(type.getBaseType().toString());
    }

    @Test
    public void testDetectAll() throws UtilityException, DetectionException {
        Path[] paths = PathUtils.listPaths(RESOURCE_PATH, null, false);

        for (Path item : paths) {
            Metadata metadata = new Metadata();
            MediaType detect = TikaHelper.detect(item, metadata);
            System.out.println(detect.toString() + " : " + item.getFileName());
        }
    }

    @Test
    public void testDetect() throws UtilityException, DetectionException {
        Path path = Paths.get("E:\\Music\\古典音乐1\\VOL29_拉威尔_色彩绚丽的名曲\\29_拉威爾_色彩絢麗的名曲_01_波麗露.mp3");
        Metadata metadata = new Metadata();
        MediaType detect = TikaHelper.detect(path, metadata);
        System.out.println(detect.toString() + " : " + path.getFileName());
    }

    @Test
    public void testDisplaySupportedTypes() {
        AutoDetectParser parser = new AutoDetectParser();
        MediaTypeRegistry registry = parser.getMediaTypeRegistry();
        Map<MediaType, Parser> parsers = parser.getParsers();

        for (MediaType type : registry.getTypes()) {
            System.out.println(type);
            for (MediaType alias : registry.getAliases(type)) {
                System.out.println("  alias:     " + alias);
            }
            MediaType supertype = registry.getSupertype(type);
            if (supertype != null) {
                System.out.println("  supertype: " + supertype);
            }
            Parser p = parsers.get(type);
            if (p != null) {
                if (p instanceof CompositeParser) {
                    p = ((CompositeParser)p).getParsers().get(type);
                }
                System.out.println("  parser:    " + p.getClass().getName());
            }
        }
    }

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
                displayDetector(sd, i+2);
            }
        }
    }

    private String indent(int indent) {
        return "                     ".substring(0, indent);
    }
}

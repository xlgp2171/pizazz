package org.pizazz2.extraction.process;

import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.TestBase;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.helper.TupleObjectHelper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractProcessorTest extends TestBase {
    static final String NAME = "sample.zip";

    @Test
    public void testExport() throws UtilityException, DetectionException, ParseException {
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream(NAME));
        ExtractProcessor processor = new ExtractProcessor();
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), NAME, "/", data);
        processor.extract(object, null);

        println(object, true, 0);
    }

    @Test
    public void testTemp() throws DetectionException, ParseException, UtilityException {
        Path path = Paths.get("E:\\Downloads\\WeChat Files\\wxid_5fvcafp07pwb22\\FileStorage\\File\\2022-03\\orcs.pdf");
        byte[] data = IOUtils.toByteArray(IOUtils.getInputStream(path));
        ExtractProcessor processor = new ExtractProcessor();
        ExtractObject object = new ExtractObject(ExtractHelper.generateId(), "think-tank.zip", "/", data);
        TupleObject config = TupleObjectHelper.newObject("encoding", "UTF8");
        processor.extract(object, config);
//        System.out.println(object);

        PathUtils.copyToPath(object.getContent().getBytes(StandardCharsets.UTF_8), Paths.get("D:/temp.txt"));
    }
}

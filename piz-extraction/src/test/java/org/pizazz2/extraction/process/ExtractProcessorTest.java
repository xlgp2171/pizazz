package org.pizazz2.extraction.process;

import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.TestBase;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;

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
}

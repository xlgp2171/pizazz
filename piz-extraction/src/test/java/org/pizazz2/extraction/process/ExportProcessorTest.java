package org.pizazz2.extraction.process;

import org.junit.Test;
import org.pizazz2.PizContext;
import org.pizazz2.common.PathUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportProcessorTest {
    @Test
    public void testExport() throws UtilityException, DetectionException, ParseException {
        Path path = Paths.get("E:\\Downloads\\yuanbo(94B86D8D355B)\\think-tank.rar" );
        byte[] data = PathUtils.toByteArray(path);
        Path temp = PathUtils.createTempDirectory(PizContext.NAMING_SHORT);
        ExtractObject object = new ExtractObject(-1L, path.getFileName().toString(), StringUtils.EMPTY, data);
        new ExtractProcessor().extract(object, null);
        new ExportProcessor(temp).exportAll(object);
    }
}

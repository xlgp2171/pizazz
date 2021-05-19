package org.pizazz2.extraction.parser;

import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.mail.MsgParser;

public class MsgParserTest {
    @Test
    public void testMain() throws DetectionException, UtilityException, ParseException {
        MsgParser parser = new MsgParser();
        byte[] data = IOUtils.toByteArray(IOUtils.getResourceAsStream("sample_charset.msg"));
        ExtractObject object = new ExtractObject(1L, "msg", StringUtils.EMPTY).setData(data);
        parser.parse(object, parser.toConfig(null), null);
        System.out.println("META:" + object.getMetadata());
        System.out.println("STATUS:" + object.getStatus());

        if (object.hasAttachment()) {
            for (ExtractObject item : object.getAttachment()) {
                System.out.println("ATTACHMENT[" + item.processed() + "]:" + item + "(" + item.getType() + ")");
            }
        }
    }
}

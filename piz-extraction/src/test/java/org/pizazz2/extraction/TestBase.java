package org.pizazz2.extraction;

import org.pizazz2.data.TupleObject;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

/**
 * 测试基类
 */
public class TestBase {
    protected void extract(TextExtraction executor, ExtractObject object, TupleObject config)
            throws DetectionException, ParseException {
        executor.extract(object, config);

        if (object.hasAttachment()) {
            for (ExtractObject item : object.getAttachment()) {
                extract(executor, item, config);
            }
        }
    }

    protected void println(ExtractObject object, boolean output, int level) {
        println(object, output, level, 64);
    }

    protected void println(ExtractObject object, boolean output, int level, int length) {
        System.out.println(multiSpace(level) + "[" + object.getStatus().name() + " (" + object.getType() + ")]:\t" +
                object + " (" + object.getSource() + ")");

        if (output) {
            String text = object.getMetadata().toString();
            System.out.println(multiSpace(level) + "META:\t" +
                    (text.length() > length ? text.substring(0, length) + " ..." : text));
            text = object.getContent();

            if (text != null) {
                text = text.replaceAll("\n", " ");
                System.out.println(multiSpace(level) + "TEXT:\t" +
                        (text.length() > length ? text.substring(0, length) + " ..." : text));
            }
        }
        if (object.hasAttachment()) {
            System.out.println(multiSpace(level) + "ATTA:\t");

            for (ExtractObject item : object.getAttachment()) {
                println(item, output, level + 1);
            }
        }
    }

    protected String multiSpace(int level) {
        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < level; i++) {
            tmp.append("\t");
        }
        return tmp.toString();
    }
}

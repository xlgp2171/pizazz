package org.pizazz2.extraction;

import org.pizazz2.data.TupleObject;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

public class TestBase {

    protected void extract(TextExtraction executor, ExtractObject object, TupleObject config) throws DetectionException,
            ParseException {
        executor.extract(object, config);

        if (object.hasAttachment()) {
            for (ExtractObject item : object.getAttachment()) {
                extract(executor, item, config);
            }
        }
    }

    protected void println(ExtractObject object, boolean output, int level) {
        System.out.println(multiSpace(level) + "[" + object.getStatus().name() + " / " + object.getType() + "]: " +
                object + "(" + object.getSource() + ")");

        if (output) {
            String text = object.getMetadata().toString();
            System.out.println(multiSpace(level) + "META: " +
                    (text.length() > 40 ? text.substring(0, 40) + " ..." : text));
            text = object.getContent();
            System.out.println(multiSpace(level) + "TEXT:" +
                    (text == null ? "None" : (text.length() > 40 ? text.substring(0, 40) + " ..." : text)));
        }
        if (object.hasAttachment()) {
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

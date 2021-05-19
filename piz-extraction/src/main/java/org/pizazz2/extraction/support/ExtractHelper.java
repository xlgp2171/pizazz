package org.pizazz2.extraction.support;

import org.apache.tika.metadata.Metadata;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.tool.IdBuilder;
import org.pizazz2.tool.IdFactory;

import java.nio.file.Path;

/**
 * 提取辅助工具
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class ExtractHelper {
    static final String WINDOWS_PATH_SEPARATOR = "\\";
    static final String PATH_DIRECTORY = "/";

    static final IdBuilder ID = IdFactory.newInstance(new Integer(32).shortValue());

    public static String pathResolve(Path parent, Path target) {
        Path tmp;

        if (parent == null) {
            tmp = target.getParent();
        } else {
            tmp = parent.resolve(target).getParent();
        }
        return tmp == null ? StringUtils.EMPTY : tmp.toString();
    }

    public static String pathFormat(String path, boolean isDirectory) {
        if (!StringUtils.isTrimEmpty(path) && path.contains(WINDOWS_PATH_SEPARATOR)) {
            path = path.replaceAll(WINDOWS_PATH_SEPARATOR + WINDOWS_PATH_SEPARATOR, PATH_DIRECTORY);
        }
        if (isDirectory && !path.endsWith(PATH_DIRECTORY)) {
            path += PATH_DIRECTORY;
        }
        return path;
    }

    public static String tryCleanBlankLine(String content, boolean cleanLine) {
        if (cleanLine && !StringUtils.isTrimEmpty(content)) {
            StringBuilder result = new StringBuilder(content.length());
            char[] charArray = content.toCharArray();
            // 计数器
            int count = 0;

            for (char item : charArray) {
                if (item == '\r') {
                    continue;
                } else if (item == '\n') {
                    count++;

                    if (count > 2) {
                        continue;
                    }
                } else {
                    count = 0;
                }
                result.append(item);
            }
            content = result.toString();
        }
        return StringUtils.nullToEmpty(content);
    }

    public static long generateId() throws ValidateException {
        return ID.generate();
    }

    public static ExtractObject newTempObject() {
        return new ExtractObject(-1L, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public static ExtractObject addAttachment(ExtractObject object, String name, String source, Metadata metadata) {
        long id = ExtractHelper.generateId();
        ExtractObject tmp = new ExtractObject(id,
                StringUtils.isEmpty(name) ? StringUtils.of(id) : name, source, metadata);
        object.addAttachment(tmp);
        return tmp;
    }
}

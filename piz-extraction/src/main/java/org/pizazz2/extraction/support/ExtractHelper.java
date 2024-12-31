package org.pizazz2.extraction.support;

import org.apache.tika.metadata.Metadata;
import org.pizazz2.PizContext;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.tool.IdBuilder;
import org.pizazz2.tool.IdFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 提取辅助工具
 *
 * @author xlgp2171
 * @version 2.2.241231
 */
public class ExtractHelper {
    public static final String WINDOWS_PATH_SEPARATOR = "\\";
    public static final String PATH_DIRECTORY = "/";

    static IdBuilder ID_BUILDER;

    static {
        String tmp = SystemUtils.getSystemProperty(PizContext.NAMING_SHORT + ".node.id", StringUtils.EMPTY);
        // 默认节点为1
        ID_BUILDER = IdFactory.newInstance(NumberUtils.toShort(tmp, NumberUtils.ONE.shortValue()));
    }

    public static Path fillPath(ExtractObject target, boolean idNamedDirectory) {
        if (target.getSource() == null) {
            return null;
        }
        String path;

        if (idNamedDirectory) {
            path = target.getId();
        } else {
            path = target.getName();
            int idx = path.lastIndexOf(".");
            path = idx != -1 ? path.substring(0, idx) : path;
        }
        return Paths.get(target.getSource(), path);
    }

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
        if (isDirectory && !path.endsWith(PATH_DIRECTORY) && !StringUtils.isTrimEmpty(path)) {
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

    public static String generateId() throws ValidateException {
        return StringUtils.of(ID_BUILDER.generate());
    }

    public static ExtractObject newTempObject() {
        return new ExtractObject("-1", StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public static ExtractObject addAttachment(ExtractObject object, String name, String source, Metadata metadata) {
        String id = ExtractHelper.generateId();
        ExtractObject tmp = new ExtractObject(id, StringUtils.isEmpty(name) ? id : name, source, metadata);
        object.addAttachment(tmp);
        return tmp;
    }

    /**
     * 修改抽取对象内容
     * @param object 抽取对象
     * @param content 内容
     */
    public static void updateContent(ExtractObject object, String content) {
        if (object != null) {
            object.forceUpdateContent(content);
        }
    }
}

package org.pizazz2.extraction.support;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.TikaProcessor;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Tika辅助工具
 *
 * @author xlgp2171
 * @version 2.1.220715
 */
public class TikaHelper {
    /** 默认的字符置信度匹配长度 */
    static final int CONFIDENCE = 35;

    public static MediaType detect(Path path, Metadata metadata)
            throws UtilityException, DetectionException, ValidateException {
        byte[] tmp = PathUtils.toByteArray(path);
        return TikaHelper.detect(tmp, metadata);
    }

    public static MediaType detect(byte[] data, Metadata metadata) throws DetectionException, ValidateException {
        return Singleton.INSTANCE.get().detect(data, metadata);
    }

    public static Charset detect(byte[] data, int limit, Charset defCharset) {
        if (limit >= 0 && !ArrayUtils.isEmpty(data)) {
            CharsetDetector tmp = limit == 0 ? new CharsetDetector() : new CharsetDetector(limit);
            CharsetMatch match = null;
            try {
                match = tmp.setText(data).detect();
            } catch (Exception e) {
                // do nothing
            }
            if (match != null && match.getConfidence() > CONFIDENCE) {
                if (Charset.isSupported(match.getName())) {
                    return Charset.forName(match.getName());
                }
            }
        }
        return defCharset;
    }

    public static String extractToText(Path path, Metadata metadata, Charset charset)
            throws UtilityException, ParseException, ValidateException {
        byte[] tmp = PathUtils.toByteArray(path);
        return TikaHelper.extractToText(tmp, metadata, charset);
    }

    public static String extractToText(byte[] data, Metadata metadata, Charset charset)
            throws ParseException, ValidateException {
        return Singleton.INSTANCE.get().extract(data, metadata, charset, TikaProcessor.HandlerEnum.TEXT);
    }

    public static String extractToHtml(Path path, Metadata metadata, Charset charset)
            throws UtilityException, ParseException, ValidateException {
        byte[] tmp = PathUtils.toByteArray(path);
        return TikaHelper.extractToHtml(tmp, metadata, charset);
    }

    public static String extractToHtml(byte[] data, Metadata metadata, Charset charset)
            throws ParseException, ValidateException {
        return Singleton.INSTANCE.get().extract(data, metadata, charset, TikaProcessor.HandlerEnum.HTML);
    }

    public static String extractToXml(Path path, Metadata metadata, Charset charset)
            throws UtilityException, ParseException, ValidateException {
        byte[] tmp = PathUtils.toByteArray(path);
        return TikaHelper.extractToXml(tmp, metadata, charset);
    }

    public static String extractToXml(byte[] data, Metadata metadata, Charset charset)
            throws ParseException, ValidateException {
        return Singleton.INSTANCE.get().extract(data, metadata, charset, TikaProcessor.HandlerEnum.XML);
    }

    public static String extract(byte[] data, Metadata metadata, Charset charset, TikaProcessor.HandlerEnum handler)
            throws ParseException {
        return Singleton.INSTANCE.get().extract(data, metadata, charset, handler);
    }

    private enum Singleton {
        /** 单例 */
        INSTANCE;

        private final TikaProcessor executor;

        Singleton() {
            executor = new TikaProcessor();
        }

        public TikaProcessor get() {
            return executor;
        }
    }
}

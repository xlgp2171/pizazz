package org.pizazz2.extraction.config;

import org.pizazz2.common.CollectionUtils;

import java.util.*;

/**
 * 类型枚举
 *
 * @author xlgp2171
 * @version 2.3.241230
 */
public enum TypeEnum {
    /** 文档 */
    DOCUMENT(new MapObject("application/rtf", "rtf").append("application/msword", "doc")
            .append("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx")
            .append("application/vnd.ms-excel", "xls")
            .append("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx")
            .append("application/vnd.ms-powerpoint", "ppt")
            .append("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx")
            .append("application/xml", "xml").append("text/csv", "csv").append("application/pdf", "pdf")
            .append("text/plain", "txt").append("text/html", "html").append("application/xhtml+xml", "xhtml")
            .append("text/x-web-markdown", "md").append("text/x-vcard", "vcf").append("application/x-hwp", "hwp")
            .append("application/x-hwp", "hwp").append("application/x-hwp-v5", "hwp")),
    /** 电子邮件 */
    EMAIL(new MapObject("application/vnd.ms-outlook", "msg").append("message/rfc822", "eml")),
    /** 音频 */
    AUDIO(new MapObject("audio/vnd.wave", "wav").append("audio/vorbis", "ogg").append("audio/mpeg", "mp3")
            .append("audio/x-flac", "flac").append("audio/x-aac", "aac").append("audio/amr", "amr")
            .append("audio/mp4", "m4a")),
    /** 视频 */
    VIDEO(new MapObject("video/x-msvideo", "avi").append("video/x-ms-wmv", "wmv").append("video/x-flv", "flv")
            .append(" video/webm", "webm").append("video/mpeg", "mpg").append("video/mp4", "mp4")
            .append("video/quicktime", "mov").append("video/3gpp", "3gp").append("video/x-ms-asf", "3gp")
            .append("video/x-matroska", "mkv")),
    /** 图像 */
    IMAGE(new MapObject("image/avif", "avif").append("image/bmp", "bmp").append("image/gif", "gif")
            .append("image/vnd.microsoft.icon", "ico").append("image/jpeg", "jpeg").append("image/png", "png")
            .append("image/svg+xml", "svg").append("image/tiff", "tiff").append("image/webp", "webp")),
    /** 压缩文件 */
    COMPRESS(new MapObject("application/x-rar-compressed; version=4", "rar")
            .append("application/x-rar-compressed; version=5", "rar").append("application/x-rar-compressed", "rar")
            .append("application/x-bzip2", "bz2").append("application/gzip", "gz")
            .append("application/x-7z-compressed", "7z").append("application/x-tar", "tar")
            .append("application/x-gtar", "tar").append("application/zip", "zip").append("image/webp", "webp"));

    private final MapObject type;

    TypeEnum(MapObject type) {
        this.type = type;
    }

    Map<String, String> getType() {
        return type.get();
    }

    public Set<String> getExtractTypes() {
        return getType().keySet();
    }

    public String getSuffix(String key) {
        return contains(key) ? getType().get(key).split("/")[0] : "";
    }

    public boolean contains(String key) {
        return getType().containsKey(key);
    }

    public void append(String key, String value) {
        type.append(key, value);
    }

    public static TypeEnum fromType(String typeString) throws IllegalArgumentException {
        for (TypeEnum item : values()) {
            if (item.contains(typeString)) {
                return item;
            }
        }
        throw new IllegalArgumentException(typeString);
    }

    static class MapObject {
        private final Map<String, String> mapping;

        public MapObject(String key, String value) {
            this.mapping = new HashMap<>();
            append(key, value);
        }

        public MapObject append(String key, String value) {
            mapping.put(key, value);
            return MapObject.this;
        }

        public Map<String, String> get() {
            return CollectionUtils.unmodifiableMap(mapping);
        }
    }
}



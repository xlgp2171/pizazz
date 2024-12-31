package org.pizazz2.extraction.support;


import org.apache.tika.metadata.*;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.extraction.config.TypeEnum;
import org.pizazz2.extraction.data.*;

import java.util.Arrays;

/**
 * 类型辅助工具
 *
 * @author xlgp2171
 * @version 2.3.241231
 */
public class TypeHelper {
    @SuppressWarnings({"unchecked"})
    public static <T>T toProperty(Metadata metadata, Class<T> clazz) {
        String type = metadata.get(Metadata.CONTENT_TYPE);
        String typeReal = type.split(";")[0];
        long length = NumberUtils.toLong(metadata.get(Metadata.CONTENT_LENGTH), -1L);
        FileProperty property = null;

        if (TypeEnum.DOCUMENT.contains(typeReal)) {
            property = TypeHelper.toDocumentProperty(metadata, length, type);
        } else if (TypeEnum.EMAIL.contains(typeReal)) {
            property = TypeHelper.toEmailProperty(metadata, length, type);
        }  else if (TypeEnum.AUDIO.contains(typeReal)) {
            property = TypeHelper.toAudioProperty(metadata, length, type);
        } else if (TypeEnum.VIDEO.contains(typeReal)) {
            property = TypeHelper.toVideoProperty(metadata, length, type);
        } else if (TypeEnum.IMAGE.contains(typeReal)) {
            property = TypeHelper.toImageProperty(metadata, length, type);
        } else if (TypeEnum.COMPRESS.contains(typeReal)) {
            property = TypeHelper.toCompressProperty(length, type);
        } else {
            String[] names = metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY).split("\\.");
            property = new FileProperty(length, type, names[names.length - 1]);
        }
        return (T) property;
    }

    public static DocumentProperty toDocumentProperty(Metadata metadata, long length, String type) {
        String typeReal = type.split(";")[0];
        DocumentProperty property = new DocumentProperty(length, type, TypeEnum.DOCUMENT.getSuffix(typeReal));

        if ("text/plain".equals(typeReal) || "text/html".equals(typeReal) || "text/x-vcard".equals(typeReal) ||
                "application/xhtml+xml".equals(typeReal) || "text/x-web-markdown".equals(typeReal)) {
            property.setEncoding(metadata.get(Metadata.CONTENT_ENCODING));
        }
        // 没有啥可解析的类型: "application/xml", "text/csv"
        if ("application/rtf".equals(typeReal) || "application/msword".equals(typeReal) ||
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(typeReal) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(typeReal) ||
                "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(typeReal) ||
                "application/vnd.ms-excel".equals(typeReal) || "application/vnd.ms-powerpoint".equals(typeReal)) {
            Integer page = metadata.getInt(Office.PAGE_COUNT);
            property.setPage(page != null ? page : -1);
            property.setCreator(metadata.get(DublinCore.CREATOR));
            property.setCreated(metadata.get(DublinCore.CREATED));
            property.setModifier(metadata.get(Office.LAST_AUTHOR));
            property.setModified(metadata.get(DublinCore.MODIFIED));
        } else if ("application/pdf".equals(typeReal)) {
            Integer page = metadata.getInt(PagedText.N_PAGES);
            property.setPage(page != null ? page : -1);
            property.setCreated(metadata.get(DublinCore.CREATED));
        }
        if ("application/vnd.ms-powerpoint".equals(typeReal) ||
                "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(typeReal)) {
            Integer page = metadata.getInt(PagedText.N_PAGES);
            property.setPage(page != null ? page : -1);
        }

        return property;
    }

    public static EmailProperty toEmailProperty(Metadata metadata, long length, String type) {
        EmailProperty property = new EmailProperty(length, type, TypeEnum.EMAIL.getSuffix(type));

        if ("application/vnd.ms-outlook".equals(type) || "message/rfc822".equals(type)) {
            property.setSubject(metadata.get(TikaCoreProperties.SUBJECT));
            property.setCreated(metadata.get(TikaCoreProperties.CREATED));
            property.setToEmail(metadata.getValues(Metadata.MESSAGE_TO_EMAIL));
            property.setToName(metadata.getValues(Metadata.MESSAGE_TO_NAME));
            property.setFromEmail(metadata.getValues(Metadata.MESSAGE_FROM_EMAIL));
            property.setFromName(metadata.getValues(Metadata.MESSAGE_FROM_NAME));
            property.setCcEmail(metadata.getValues(Metadata.MESSAGE_CC_EMAIL));
            property.setCcName(metadata.getValues(Metadata.MESSAGE_CC_NAME));
            property.setBccEmail(metadata.getValues(Metadata.MESSAGE_BCC_EMAIL));
            property.setBccName(metadata.getValues(Metadata.MESSAGE_BCC_NAME));
        }
        return property;
    }

    public static AudioProperty toAudioProperty(Metadata metadata, long length, String type) {
        AudioProperty property = new AudioProperty(length, type, TypeEnum.AUDIO.getSuffix(type));

        if ("audio/x-aac".equals(type) || "audio/amr".equals(type)) {
            // FIXME 该类型暂时无法解析
            return property;
        }
        if ("audio/vnd.wave".equals(type)) {
            property.setBits(NumberUtils.toInt(metadata.get("bits"), 0));
            property.setChannels(NumberUtils.toInt(metadata.get("channels"), 1));
            property.setSampleRate(NumberUtils.toInt(metadata.get(XMPDM.AUDIO_SAMPLE_RATE), 0));
            double duration = (property.getLength() - 42) * 1.0 / (property.getSampleRate() * property.getBits() *
                    property.getChannels() * 1.0 / 8);
            property.setDuration((float) duration);
        } else if ("audio/mpeg".equals(type) || "audio/vorbis".equals(type) || "audio/x-flac".equals(type) ||
                "audio/mp4".equals(type)) {
            property.setChannels(NumberUtils.toInt(metadata.get("channels"), 1));
            property.setSampleRate(NumberUtils.toInt(metadata.get(XMPDM.AUDIO_SAMPLE_RATE), 0));
            property.setDuration(NumberUtils.toFloat(metadata.get(XMPDM.DURATION), -1F));
        }
        return property;
    }

    public static VideoProperty toVideoProperty(Metadata metadata, long length, String type) {
        VideoProperty property = new VideoProperty(length, type, TypeEnum.VIDEO.getSuffix(type));

        if ("video/x-msvideo".equals(type) || "video/x-ms-wmv".equals(type) || "video/webm".equals(type) ||
                "video/mpeg".equals(type) || "video/x-matroska".equals(type)) {
            // FIXME 该类型暂时无法解析
            return property;
        }
        if ("video/x-flv".equals(type)) {
            String width = metadata.get("width");
            property.setWidth((int) NumberUtils.toDouble(width, -1D));
            String height = metadata.get("height");
            property.setHeight((int) NumberUtils.toDouble(height, -1D));
            String duration = metadata.get("duration");
            property.setDuration(NumberUtils.toFloat(duration, -1F));
        } else if ("video/mp4".equals(type) || "video/quicktime".equals(type) || "video/3gpp".equals(type)) {
            Integer width = metadata.getInt(TIFF.IMAGE_WIDTH);
            property.setWidth(width != null ? width : -1);
            Integer height = metadata.getInt(TIFF.IMAGE_LENGTH);
            property.setHeight(height != null ? height : -1);
            String duration = metadata.get(XMPDM.DURATION);
            property.setDuration(NumberUtils.toFloat(duration, -1F));
        }
        return property;
    }

    public static ImageProperty toImageProperty(Metadata metadata, long length, String type) {
        ImageProperty property = new ImageProperty(length, type, TypeEnum.IMAGE.getSuffix(type));

        if ("image/avif".equals(type) || "image/vnd.microsoft.icon".equals(type) || "image/svg+xml".equals(type)) {
            // FIXME 该类型暂时无法解析
            // 解析svg：https://blog.csdn.net/y_dzaichirou/article/details/141440478
            // 解析ico：https://blog.51cto.com/u_16213430/11758600
            return property;
        }
        if ("image/bmp".equals(type) || "image/gif".equals(type) || "image/jpeg".equals(type) ||
                "image/png".equals(type) || "image/tiff".equals(type)) {
            // 填充图片宽度
            Integer width = metadata.getInt(TIFF.IMAGE_WIDTH);
            property.setWidth(width != null ? width : -1);
            // 填充图片高度
            Integer height = metadata.getInt(TIFF.IMAGE_LENGTH);
            property.setHeight(height != null ? height : -1);
        } else if ("image/webp".equals(type)) {
            // 填充图片宽度
            String width = metadata.get("Image Width");
            property.setWidth(NumberUtils.toInt(width, -1));
            // 填充图片高度
            String height = metadata.get("Image Height");
            property.setHeight(NumberUtils.toInt(height, -1));
        }
        // 填充图片位深度
        if ("image/jpeg".equals(type)) {
            int[] bitPerSample = metadata.getIntValues(TIFF.BITS_PER_SAMPLE);
            property.setBitDepth(bitPerSample.length > 0 ? bitPerSample[0] * 3 : -1);
        } else if ("image/png".equals(type)) {
            int[] bitPerSample = metadata.getIntValues(TIFF.BITS_PER_SAMPLE);
            property.setBitDepth(bitPerSample.length > 0 ? Arrays.stream(bitPerSample).sum() : -1);
        } else if ("image/tiff".equals(type)) {
            int[] bitPerSample = metadata.getIntValues(TIFF.BITS_PER_SAMPLE);
            Integer samplesPerPixel = metadata.getInt(TIFF.SAMPLES_PER_PIXEL);
            property.setBitDepth(samplesPerPixel != null && bitPerSample.length > 0 ?
                    samplesPerPixel * bitPerSample[0] : -1);
        }
        return property;
    }

    public static CompressProperty toCompressProperty(long length, String type) {
        return new CompressProperty(length, type, TypeEnum.COMPRESS.getSuffix(type));
    }
}

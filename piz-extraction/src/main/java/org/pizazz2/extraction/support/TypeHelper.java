package org.pizazz2.extraction.support;


import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.extraction.config.TypeEnum;
import org.pizazz2.extraction.data.*;

/**
 * 类型辅助工具
 *
 * @author xlgp2171
 * @version 2.3.241028
 */
public class TypeHelper {
    @SuppressWarnings({"unchecked"})
    public static <T>T toProperty(Metadata metadata, Class<T> clazz) {
        String type = metadata.get(Metadata.CONTENT_TYPE);
        long length = NumberUtils.toLong(metadata.get(Metadata.CONTENT_LENGTH), -1L);
        AbstractProperty property = null;

        if (TypeEnum.DOCUMENT.contains(type) && clazz == DocumentProperty.class) {
            property = TypeHelper.toDocumentProperty(metadata, length, type);
        } else if (TypeEnum.EMAIL.contains(type) && clazz == EmailProperty.class) {
            property = TypeHelper.toEmailProperty(length, type);
        }  else if (TypeEnum.AUDIO.contains(type) && clazz == AudioProperty.class) {
            property = TypeHelper.toAudioProperty(metadata, length, type);
        } else if (TypeEnum.VIDEO.contains(type) && clazz == VideoProperty.class) {
            property = TypeHelper.toVideoProperty(metadata, length, type);
        } else if (TypeEnum.IMAGE.contains(type) && clazz == ImageProperty.class) {
            property = TypeHelper.toImageProperty(length, type);
        } else if (TypeEnum.COMPRESS.contains(type) && clazz == CompressProperty.class) {
            property = TypeHelper.toCompressProperty(length, type);
        }
        return (T) property;


    }

    public static DocumentProperty toDocumentProperty(Metadata metadata, long length, String type) {
        DocumentProperty property = new DocumentProperty(length, type, TypeEnum.AUDIO.getSuffix(type));

        if ("application/pdf".equals(type)) {
            property.setSize(NumberUtils.toInt(metadata.get("xmpTPg:NPages"), -1));
        }
        return property;
    }

    public static EmailProperty toEmailProperty(long length, String type) {
        EmailProperty property = new EmailProperty(length, type, TypeEnum.AUDIO.getSuffix(type));
        return property;
    }

    public static AudioProperty toAudioProperty(Metadata metadata, long length, String type) {
        AudioProperty property = new AudioProperty(length, type, TypeEnum.AUDIO.getSuffix(type));

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
        } else if ("audio/ogg".equals(type) || "audio/x-aac".equals(type) || "audio/amr".equals(type)) {
            property.setDuration(-1F);
        }
        return property;
    }

    public static VideoProperty toVideoProperty(Metadata metadata, long length, String type) {
        VideoProperty property = new VideoProperty(length, type, TypeEnum.AUDIO.getSuffix(type));

        if ("video/x-msvideo".equals(type)) {
            property.setResolution(metadata.get("videoResolution"));
            property.setDuration(metadata.get(XMPDM.DURATION));
        }

        return property;
    }

    public static ImageProperty toImageProperty(long length, String type) {
        ImageProperty property = new ImageProperty(length, type, TypeEnum.AUDIO.getSuffix(type));
        return property;
    }

    public static CompressProperty toCompressProperty(long length, String type) {
        CompressProperty property = new CompressProperty(length, type, TypeEnum.AUDIO.getSuffix(type));
        return property;
    }
}

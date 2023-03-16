package org.pizazz2.extraction.data;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.tool.ref.IData;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 文档链接流转对象
 *
 * @author xlgp2171
 * @version 2.2.230310
 */
public class ExtractObject extends LinkedObject<byte[]> implements IData {
    /** 文档属性 */
    private final Metadata metadata;
    /** 文档状态 */
    private StatusEnum status;
    /** 文档内容 */
    private String content;

    public ExtractObject(String id, String name, String source) throws ValidateException {
        this(id, name, source, (Metadata) null);
    }

    public ExtractObject(String id, String name, String source, byte[] data) throws ValidateException {
        this(id, name, source, null, data);
    }

    public ExtractObject(String id, String name, String source, Metadata metadata, byte[] data) {
        this(id, name, source, metadata);
        setData(data);
    }

    public ExtractObject(String id, String name, String source, Metadata metadata) throws ValidateException {
        this(id, name, source, metadata, new LinkedList<>());
    }

    public ExtractObject(String id, String name, String source, Metadata metadata, Collection<ExtractObject> attachment)
            throws ValidateException {
        super(id, name, source, attachment);
        this.metadata = metadata == null ? new Metadata() : metadata;
        initialMetadataName();
        reset();
    }

    private void initialMetadataName() {
        this.metadata.set(Metadata.TIKA_MIME_FILE, getName());
        this.metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, getName());
    }

    @Override
    public boolean isEmpty() {
        // 属性未空， 文档内容为空，附件为空
        return (metadata == null || metadata.size() <= 2) && StringUtils.isTrimEmpty(content) &&
                getChildren().isEmpty();
    }

    @Override
    public Object get(String key, Object defValue) {
        if (metadata != null) {
            String[] tmp = metadata.getValues(key);

            if (!ArrayUtils.isEmpty(tmp)) {
                return tmp;
            }
        }
        return defValue;
    }

    @Override
    public void reset() {
        status = StatusEnum.READY;
    }

    @SuppressWarnings("unchecked")
    public ExtractObject addAttachment(ExtractObject object) {
        if (object != null) {
            ((Collection<LinkedObject<byte[]>>) super.getChildren()).add(object);
        }
        return this;
    }

    /**
     * 是否清空元数据
     *
     * @param clear 是否清空元数据
     */
    public void archive(boolean clear) {
        if (clear) {
            setData(ArrayUtils.EMPTY_BYTE);
        }
        if (status != StatusEnum.READY) {
            // 若不为加密文档
            if (isEmpty() && status != StatusEnum.ENCRYPTION && status != StatusEnum.EMPTY) {
                // 当内容和属性都为空又没有附件时，认为文档是无效的
                status = StatusEnum.INVALID;
            }
        } else {
            if (!getChildren().isEmpty()) {
                status = StatusEnum.ATTACHMENT;
            } else if (isEmpty() && status != StatusEnum.ENCRYPTION) {
                // 当内容和属性都为空又没有附件时，认为文档是无效的
                status = StatusEnum.INVALID;
            } else {
                status = StatusEnum.FINISHED;
            }
        }
    }

    @Override
    public boolean processed() {
        return status != StatusEnum.READY;
    }

    public boolean hasAttachment() {
        return status == StatusEnum.ATTACHMENT || status == StatusEnum.BROKEN;
    }

    public ExtractObject setType(MediaType type) {
        return setTypeString(type == null ? StringUtils.EMPTY : StringUtils.of(type));
    }

    @Override
    @Deprecated
    public LinkedObject<byte[]> setClassification(String classification) {
        return this;
    }

    @Override
    public ExtractObject setData(byte[] data) {
        return (ExtractObject) super.setData(data);
    }

    public ExtractObject setTypeString(String type) {
        if (!processed() && !StringUtils.isEmpty(type)) {
            super.setClassification(type);
            this.getMetadata().set(Metadata.CONTENT_TYPE, type);
        }
        return this;
    }

    public ExtractObject setContent(String content) {
        if (!processed() && !StringUtils.isEmpty(content)) {
            this.content = content.trim();
        }
        return this;
    }

    public ExtractObject setStatus(StatusEnum status) {
        if (status != null) {
            this.status = status;
        }
        return this;
    }

    @Override
    public String getSource() {
        return ExtractHelper.pathFormat(super.getSource(), true);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @SuppressWarnings("unchecked")
    public Collection<ExtractObject> getAttachment() {
        return (Collection<ExtractObject>) getChildren();
    }

    public MediaType getType() {
        if (StringUtils.isEmpty(getClassification())) {
            return null;
        } else {
            return MediaType.parse(getClassification());
        }
    }

    public String getTypeString() {
        return getClassification();
    }

    public StatusEnum getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int length() {
        return ArrayUtils.isEmpty(super.getData()) ? NumberUtils.ZERO.intValue() : getData().length;
    }

    public enum StatusEnum {
        /** 文档初始状态 */
        READY(0),
        /** 文档处理完成 */
        FINISHED(1),
        /** 文档包含附件 */
        ATTACHMENT(2),
        /** 文档内容为空 */
        EMPTY(3),
        /** 文档被加密 */
        ENCRYPTION(4),
        /** 文档无效 */
        INVALID(-1),
        /** 文档附件损坏 */
        BROKEN(-2),
        /** 文档无法解析 */
        UNSUPPORTED(-3),
        /** 文档被舍弃 */
        REJECT(-4);

        private final int value;

        StatusEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static StatusEnum from(int status) {
            for (StatusEnum item : values()) {
                if (item.getValue() == status) {
                    return item;
                }
            }
            return StatusEnum.UNSUPPORTED;
        }
    }
}

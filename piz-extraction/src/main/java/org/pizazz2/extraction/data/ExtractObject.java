package org.pizazz2.extraction.data;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.LinkedObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.support.ExtractHelper;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 文档链接流转对象
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class ExtractObject extends LinkedObject<byte[]> {
    /**
     * 文档属性
     */
    private final Metadata metadata;

    /**
     * 文档状态
     */
    private StatusEnum status;
    /**
     * 文档内容
     */
    private String content;

    public ExtractObject(long id, String name, String source) throws ValidateException {
        this(id, name, source, (Metadata) null);
    }

    public ExtractObject(long id, String name, String source, byte[] data) throws ValidateException {
        this(id, name, source, null, data);
    }

    public ExtractObject(long id, String name, String source, Metadata metadata, byte[] data) {
        this(id, name, source, metadata);
        setData(data);
    }

    public ExtractObject(long id, String name, String source, Metadata metadata) throws ValidateException {
        this(id, name, source, metadata, new LinkedList<>());
    }

    public ExtractObject(long id, String name, String source, Metadata metadata, Collection<ExtractObject> attachment)
            throws ValidateException {
        super(id, name, source, attachment);
        this.metadata = metadata == null ? new Metadata() : metadata;
        initialMetadataName();
        reset();
    }

    private void initialMetadataName() {
        this.metadata.set(Metadata.TIKA_MIME_FILE, getName());
        this.metadata.set(Metadata.RESOURCE_NAME_KEY, getName());
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
                // 当内容和属性都为空又没有附件时，认为文档是空的
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

    public enum StatusEnum {
        /**
         * 初始的文档
         */
        READY(0),
        /**
         * 处理完成的文档
         */
        FINISHED(1),
        /**
         * 含有附件的文档
         */
        ATTACHMENT(2),
        /**
         * 空值的文档
         */
        EMPTY(3),
        /**
         * 加密的文档
         */
        ENCRYPTION(4),
        /**
         * 无效的文档
         */
        INVALID(-1),
        /**
         * 附件不全的文档
         */
        BROKEN(-2),
        /**
         * 不支持的文档/损坏的文档
         */
        UNSUPPORTED(-3),
        /**
         * 未知的文档
         */
        UNKNOWN(-4);

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

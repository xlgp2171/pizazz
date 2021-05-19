package org.pizazz2.extraction;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.pizazz2.ICloseable;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.ExtractConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ConcurrentProcessor;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.process.ExtractProcessor;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.TikaHelper;
import org.pizazz2.helper.TupleObjectHelper;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 文本提取
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public class TextExtraction implements ICloseable {
    private final ExtractProcessor extract;
    private final ConcurrentProcessor concurrent;
    private final ExtractConfig extractConfig;
    private BiFunction<TupleObject, MediaType, TupleObject> modify;

    public TextExtraction() throws ValidateException {
        this(-1);
    }

    /**
     *
     * @param parallelism 并行线程数(数字正数表示线程数，否则不采用并行处理器)
     *
     * @throws ValidateException 验证异常
     */
    public TextExtraction(int parallelism) throws ValidateException {
        this(parallelism, null, null, null);
    }

    public TextExtraction(int parallelism, IExtractListener listener) throws ValidateException {
        this(parallelism, null, null, listener);
    }

    public TextExtraction(int parallelism, Predicate<MediaType> defaultCheck, BiFunction<TupleObject,
            MediaType, TupleObject> defaultModify, IExtractListener listener) throws ValidateException {
        extract = new ExtractProcessor(extractConfig = new ExtractConfig(), defaultCheck, defaultModify, listener);
        // 至少并行数为2
        if (parallelism > 1) {
            concurrent = new ConcurrentProcessor(extract, parallelism);
        } else {
            concurrent = null;
        }
    }

    public MediaType detect(ExtractObject object) throws DetectionException, ValidateException {
        ValidateUtils.notNull("detect", object);
        return object.setType(detect(object.getData(), object.getMetadata())).getType();
    }

    public MediaType detect(byte[] data, Metadata metadata) throws DetectionException, ValidateException {
        ValidateUtils.notNull("detect", data, metadata);
        return TikaHelper.detect(data, metadata);
    }

    public ExtractObject extract(String name, String source, byte[] data) throws DetectionException, ParseException,
            ValidateException {
        return extract(name, source, data, TupleObjectHelper.emptyObject());
    }

    public ExtractObject extract(String name, String source, byte[] data, TupleObject config) throws DetectionException,
            ParseException, ValidateException {
        return extract(new ExtractObject(ExtractHelper.generateId(), name, source, data), config);
    }

    public ExtractObject extract(ExtractObject object) throws DetectionException, ParseException, ValidateException {
        return extract(object, TupleObjectHelper.emptyObject());
    }

    public ExtractObject extract(ExtractObject object, TupleObject config) throws DetectionException, ParseException,
            ValidateException {
        extract.extract(object, config, modify);

        if (object.hasAttachment()) {
            if (concurrent != null) {
                concurrent.executeBatch(object.getAttachment(), config);
            } else {
                for (ExtractObject item : object.getAttachment()) {
                    extract(item, config);
                }
            }
        }
        return object;
    }

    public boolean extract(Collection<ExtractObject> collection, TupleObject config) {
        if (concurrent != null) {
            concurrent.executeBatch(collection, config);
            return true;
        }
        return false;
    }

    /**
     * 设置配置修改器<br>
     * 运行在类型识别完成后，解析object之前，可修改解析输入配置
     *
     * @param modify 配置修改器
     */
    public void setModify(BiFunction<TupleObject, MediaType, TupleObject> modify) {
        this.modify = modify;

        if (concurrent != null) {
            concurrent.setModify(modify);
        }
    }

    public ExtractProcessor getExtract() {
        return extract;
    }

    public ConcurrentProcessor getConcurrent() {
        return concurrent;
    }

    public ExtractConfig getExtractConfig() {
        return extractConfig;
    }

    @Override
    public void destroy(Duration timeout) {
        SystemUtils.destroy(concurrent, timeout);
    }
}

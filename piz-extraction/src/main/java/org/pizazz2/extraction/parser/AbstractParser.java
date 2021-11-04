package org.pizazz2.extraction.parser;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.ExtractConfig;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.config.ParseConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.CodeEnum;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ExtractProcessor;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.TikaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * 执行器基类
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public abstract class AbstractParser implements IParser {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);
    private ExtractProcessor processor;
    private ExtractConfig extractConfig;
    /**
     * 解析实现
     *
     * @param object 详情对象
     * @param config 操作配置
     * @param listener 监听
     *
     * @throws ParseException 解析异常
     * @throws ValidateException 验证异常
     * @throws DetectionException 识别异常
     */
    protected abstract void doParse(ExtractObject object, IConfig config, IExtractListener listener)
            throws ParseException, ValidateException, DetectionException;

    @Override
    public final void parse(ExtractObject object, IConfig config, IExtractListener listener)
            throws ParseException, ValidateException, IllegalException, DetectionException {
        if (object.processed()) {
            return;
        } else if (ArrayUtils.isEmpty(object.getData())) {
            object.setStatus(ExtractObject.StatusEnum.EMPTY);
            return;
        }
        try {
            doParse(object, config, listener);
            listener.parsed(object, config);
        } catch(ParseException | DetectionException e) {
            listener.exception(object, e);
            throw e;
        } catch (Exception e) {
            listener.exception(object, e);
            object.setStatus(ExtractObject.StatusEnum.UNKNOWN);
        } finally {
            // 归档操作
            object.archive(config.cleanData());
        }
    }

    @Override
    public IConfig toConfig(TupleObject config) throws IllegalException {
        return new ParseConfig(config);
    }

    protected Charset detect(byte[] value, int limit, Charset defCharset) {
        value = ArrayUtils.nullToEmpty(value);
        return TikaHelper.detect(value, limit, defCharset);
    }

    protected MediaType detect(ExtractObject object) throws DetectionException {
        return processor.detect(object);
    }

    protected String extract(ExtractObject object, TupleObject config) throws DetectionException, ParseException,
            ValidateException, IllegalException {
        return processor.extract(object, config);
    }

    protected void throwException(ExtractObject object, IConfig config, Exception e) throws ParseException {
        object.setStatus(ExtractObject.StatusEnum.UNKNOWN);
        String msg = "PARSE ERROR:" + e.getMessage() + ",id=" + object + ",type=" + object.getType() + ",length=" +
                (object.getData() == null ? "NaN" : object.getData().length);
        object.setContent(msg);

        if (!config.ignoreException()) {
            throw new ParseException(CodeEnum.ETT_03, msg, e);
        } else {
            LOGGER.warn(msg);
        }
    }

    protected ExtractObject addAttachment(ExtractObject object, String name, String source) {
        return ExtractHelper.addAttachment(object, name, source, new Metadata());
    }

    @Override
    public IParser setUp(ExtractProcessor processor, ExtractConfig extractConfig) {
        this.processor = processor;
        this.extractConfig = extractConfig;
        return this;
    }

    protected ExtractConfig getExtractConfig() {
        return extractConfig;
    }
}

package org.pizazz2.extraction.process;

import org.apache.tika.mime.MediaType;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.ExtractConfig;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.parser.IParser;
import org.pizazz2.extraction.support.TikaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 提取处理器
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public class ExtractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractProcessor.class);
    private static final Map<MediaType, IParser> PARSER_CACHE;

    private final ExtractConfig extractConfig;
    private final Predicate<MediaType> defaultCheck;
    private final BiFunction<TupleObject, MediaType, TupleObject> defaultModify;
    private final IExtractListener listener;
    private final MediaType defaultType;

    static {
        Map<MediaType, IParser> tmp = new HashMap<>();
        // 加载解析器
        for (IParser item : ServiceLoader.load(IParser.class, ClassUtils.getClassLoader())) {
            for (String type : item.getType()) {
                tmp.put(MediaType.parse(type), item);
                LOGGER.info("PARSER LOADED:" + type + ",impl=" + item.getClass().getName());
            }
        }
        PARSER_CACHE = CollectionUtils.unmodifiableMap(tmp);
    }

    public ExtractProcessor() throws ValidateException {
        this(new ExtractConfig(), null, null, null);
    }

    public ExtractProcessor(ExtractConfig extractConfig, Predicate<MediaType> defaultCheck, BiFunction<TupleObject,
            MediaType, TupleObject> defaultModify, IExtractListener listener) throws ValidateException {
        this(extractConfig, defaultCheck == null ? type -> {
            // 白名单为空时 通过验证
            if (extractConfig.getTypeWhiteList().isEmpty()) {
                return true;
            } else {
                // 白名单有类型时，通过验证
                return extractConfig.getTypeWhiteList().contains(type.getBaseType().toString());
            }
        } : defaultCheck, defaultModify == null ? (config, type) -> config : defaultModify, listener,
                MediaType.parse("text/tika"));
    }

    public ExtractProcessor(ExtractConfig extractConfig, Predicate<MediaType> defaultCheck, BiFunction<TupleObject,
            MediaType, TupleObject> defaultModify, IExtractListener listener, MediaType defaultType)
            throws ValidateException {
        ValidateUtils.notNull("ExtractProcessor", extractConfig, defaultCheck, defaultModify, true,
                defaultType);
        this.extractConfig = extractConfig;
        this.defaultCheck = defaultCheck;
        this.defaultModify = defaultModify;
        this.listener = new ProxyListener(listener);
        this.defaultType = defaultType;
    }

    public MediaType detect(ExtractObject object) throws DetectionException {
        if (object.getType() == null) {
            try {
                object.setType(TikaHelper.detect(object.getData(), object.getMetadata()));
            } catch (ValidateException e) {
                object.setStatus(ExtractObject.StatusEnum.EMPTY);
                object.archive(false);
            }
            // 调用后才作响应
            listener.detected(object);
        }

        return object.getType();
    }

    public String extract(ExtractObject object, TupleObject config) throws DetectionException, ParseException,
            ValidateException, IllegalException {
        return extract(object, config, null);
    }

    public String extract(ExtractObject object, TupleObject config, BiFunction<TupleObject, MediaType,
            TupleObject> modify) throws DetectionException, ParseException, ValidateException, IllegalException {
        ValidateUtils.notNull("extract", object);

        if (detect(object) != null) {
            if (!defaultCheck.test(object.getType())) {
                object.setStatus(ExtractObject.StatusEnum.UNSUPPORTED);
                listener.extracted(object);
                return StringUtils.EMPTY;
            }
            IParser parser = getParser(object.getType());
            // 可以根据配置和预测类型调整配置参数
            if (modify != null) {
                config = modify.apply(config, object.getType());
            } else {
                config = defaultModify.apply(config, object.getType());
            }
            parser.parse(object, parser.toConfig(config), listener);
        }
        // 任何情况都作响应
        listener.extracted(object);
        return object.getContent();
    }

    public boolean hasParser(MediaType type) {
        return PARSER_CACHE.containsKey(type.getBaseType());
    }

    public IParser getParser(MediaType type) {
        if (hasParser(type)) {
            return PARSER_CACHE.get(type.getBaseType()).setUp(this, extractConfig);
        } else {
            return defaultParser();
        }
    }

    public IParser defaultParser() {
        if (hasParser(defaultType)) {
            return getParser(defaultType);
        } else {
            return new UnsupportedParser();
        }
    }

    static class UnsupportedParser extends AbstractParser {

        @Override
        protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
                ValidateException, DetectionException {
            object.setStatus(ExtractObject.StatusEnum.UNSUPPORTED);
            listener.parsed(object, config);
        }

        @Override
        public String[] getType() {
            return new String[] { "application/empty" };
        }
    }

    static class ProxyListener implements IExtractListener {
        private final IExtractListener listener;

        ProxyListener(IExtractListener listener) {
            this.listener = listener == null ? target -> {} : listener;
        }

        @Override
        public void detected(ExtractObject target) {
            listener.detected(target);
        }

        @Override
        public void parsed(ExtractObject target, IConfig config) {
            listener.parsed(target, config);
        }

        @Override
        public void exception(ExtractObject target, Exception e) {
            listener.exception(target, e);
        }

        @Override
        public void extracted(ExtractObject target) {
            listener.extracted(target);
        }
    }
}

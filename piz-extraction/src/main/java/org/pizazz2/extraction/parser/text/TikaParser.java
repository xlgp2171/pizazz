package org.pizazz2.extraction.parser.text;

import org.apache.tika.mime.MediaType;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.config.ParseConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.process.TikaProcessor;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.TikaHelper;
import org.pizazz2.helper.TupleObjectHelper;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 通用(text/tika)解析<br>
 * 解析属性Metadata由数据类型决定
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public class TikaParser extends AbstractParser {
    static final MediaType TYPE_DEFAULT = MediaType.parse("text/plain");
    static final Set<String> FORMAT_DISCOVERY;

    static {
        FORMAT_DISCOVERY = CollectionUtils.unmodifiableSet(new HashSet<>(
                Collections.singletonList("application/octet-stream")));
    }

    @Override
    public IConfig toConfig(TupleObject config) {
        return new TikaParser.Config(config);
    }

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, IllegalException, DetectionException {
        Config tmp = config.getTarget(Config.class);
        TikaProcessor.HandlerEnum handler = TikaProcessor.HandlerEnum.from(tmp.textFormat());
        String content = null;

        if (handler != null) {
            discover(object, tmp);
            content = TikaHelper.extract(object.getData(), object.getMetadata(), config.charset(), handler);
        } else {
            object.setStatus(ExtractObject.StatusEnum.UNKNOWN);
        }
        if (content != null) {
            object.setContent(ExtractHelper.tryCleanBlankLine(content, config.cleanLine()));
        }
    }

    private void discover(ExtractObject object, Config config) throws DetectionException {
        if (FORMAT_DISCOVERY.contains(StringUtils.of(object.getType()).toLowerCase())) {
            // 文件流需要再次识别编码方式
            Charset charset = super.detect(object.getData(), config.detectLimit(), null);
            // 若编码方式有效
            if (charset != null && config.getDiscoverEncoding().contains(charset.name())) {
                // 过滤有意义的编码方式 并按照文本方式进行识别，默认包含GB2312/GBK/GB18030/BIG5
                object.setType(TYPE_DEFAULT);
            }
        }
    }

    @Override
    public String[] getType() {
        return new String[] { "text/tika" };
    }

    public static class Config extends ParseConfig {
        /**
         * 输出文本格式
         */
        private final String textFormat;
        private final Set<String> discoverEncoding;

        public Config(TupleObject config) throws IllegalException {
            super(config);
            this.textFormat = TupleObjectHelper.getString(config, "textFormat", "text");
            String discover = TupleObjectHelper.getString(config, "discoverEncoding",
                    "GB2312,GBK,GB18030,BIG5");
            discoverEncoding = new HashSet<>(Arrays.asList(discover.split(",")));
        }

        public String textFormat() {
            return textFormat;
        }

        public Set<String> getDiscoverEncoding() {
            return discoverEncoding;
        }
    }
}

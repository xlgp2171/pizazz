package org.pizazz2.extraction.config;

import org.pizazz2.PizContext;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.TupleObjectHelper;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 配置基类
 *
 * @author xlgp2171
 * @version 2.1.211104
 */
public class ParseConfig implements IConfig {
    private final boolean ignoreException;
    private final int detectLimit;
    private final boolean cleanData;
    private final boolean cleanLine;
    private final Charset charset;

    public ParseConfig(TupleObject config) throws IllegalException {
        // 默认不忽略异常
        this.ignoreException = TupleObjectHelper.getBoolean(config, "ignoreException", Boolean.FALSE);
        // 默认最多检查1MB()
        this.detectLimit = TupleObjectHelper.getInt(config, "detectLimit", 1024 * 1024);
        // 默认在操作后清空元数据
        this.cleanData = TupleObjectHelper.getBoolean(config, "cleanData", Boolean.TRUE);
        // 默认清除多余的空行
        this.cleanLine = TupleObjectHelper.getBoolean(config,"cleanLine", Boolean.TRUE);
        // 默认按照系统编码格式
        this.charset = toCharset(config);
    }

    private Charset toCharset(TupleObject config) throws IllegalException {
        String encoding = TupleObjectHelper.getString(config, "encoding", PizContext.LOCAL_ENCODING.name());
        Charset tmp;
        try {
            tmp = Charset.forName(encoding);
        } catch (UnsupportedCharsetException e) {
            if (!ignoreException()) {
                throw new IllegalException("UNSUPPORTED CHARSET:" + encoding, e);
            } else {
                tmp = PizContext.LOCAL_ENCODING;
            }
        }
        return tmp;
    }

    @Override
    public boolean ignoreException() {
        return ignoreException;
    }

    @Override
    public int detectLimit() {
        return detectLimit;
    }

    @Override
    public boolean cleanData() {
        return cleanData;
    }

    @Override
    public boolean cleanLine() {
        return cleanLine;
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getTarget(Class<? extends IConfig> type) throws ValidateException, IllegalException {
        return (T) ClassUtils.cast(this, type);
    }
}

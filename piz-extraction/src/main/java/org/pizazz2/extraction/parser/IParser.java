package org.pizazz2.extraction.parser;

import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.ExtractConfig;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.process.ExtractProcessor;

/**
 * 解析器接口
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public interface IParser {
    /**
     * 转换为解析器配置
     *
     * @param config 配置
     * @return 解析器配置
     */
    IConfig toConfig(TupleObject config);

    /**
     * 解析文档
     *
     * @param object 详情对象
     * @param config 操作配置
     * @param listener 监听
     *
     * @throws ParseException 解析异常
     * @throws DetectionException 识别异常
     * @throws ValidateException 验证异常
     */
    void parse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            DetectionException, ValidateException;

    /**
     * 装配解析器和配置
     *
     * @param processor 提取解析器
     * @param extractConfig 提取全局配置
     * @return 当前对象
     */
    IParser setUp(ExtractProcessor processor, ExtractConfig extractConfig);

    /**
     * 获取解析类型
     * @return 解析类型
     */
    String[] getType();
}

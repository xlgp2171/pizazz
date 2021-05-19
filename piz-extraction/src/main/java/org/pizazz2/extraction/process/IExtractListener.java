package org.pizazz2.extraction.process;

import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;

/**
 * 流程监听
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
@FunctionalInterface
public interface IExtractListener {
    /**
     * 识别文档后响应<br>
     * 经过识别操作才会被响应
     *
     * @param target 文档对象
     */
    default void detected(ExtractObject target) {
    }

    /**
     * 解析文档后响应<br>
     * 经过解析正确后才会响应
     *
     * @param target 文档对象
     * @param config 当前解析配置
     */
    default void parsed(ExtractObject target, IConfig config) {
    }

    /**
     * 解析异常响应
     *
     * @param target 文档对象
     * @param e 异常
     */
    default void exception(ExtractObject target, Exception e) {
    }

    /**
     * 抽取完成响应<br>
     * 每个抽取任务必然会响应，
     *
     * @param target 文档对象
     */
    void extracted(ExtractObject target);
}

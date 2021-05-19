package org.pizazz2.extraction.config;

import org.pizazz2.common.ClassUtils;
import org.pizazz2.exception.ValidateException;

import java.nio.charset.Charset;

/**
 * 解析配置接口
 *
 * @author xlgp2171
 * @version 2.0.210401
 */
public interface IConfig {
    /**
     * 是否忽略解析中的异常
     *
     * @return 是否忽略解析中的异常
     */
    boolean ignoreException();

    /**
     * 识别数据中编码格式的长度
     * <li>小于等于0为默认值
     * <li>正数为长度
     *
     * @return 识别数据长度
     */
    int detectLimit();

    /**
     * 是否清除元数据(数据处理完成之后)
     *
     * @return 是否清除元数据
     */
    boolean cleanData();

    /**
     * 是否清除多余的空行(保留空行于\n\n)
     *
     * @return 是否清除多余的空行
     */
    boolean cleanLine();
    /**
     * 获取设置的编码格式
     *
     * @return 编码格式
     */
    Charset charset();

    /**
     * 接口转实现类
     *
     * @param type 实现IConfig接口的类
     * @param <T> 实现了IConfig接口的配置对象
     * @return 实现了IConfig接口的配置对象
     *
     * @throws ValidateException 验证异常
     */
    <T> T getTarget(Class<? extends IConfig> type) throws ValidateException;
}

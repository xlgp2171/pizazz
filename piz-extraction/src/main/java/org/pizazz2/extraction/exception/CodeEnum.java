package org.pizazz2.extraction.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 错误码
 *
 * @author xlgp2171
 * @version 2.2.240627
 */
public enum CodeEnum implements IMessageCode {
    /** 读取IO异常 */
    ETT_01("ETT01#"),
    /** 无法解析对应的类型 */
    ETT_02("ETT02#"),
    /** 解析器内容解析异常 */
    ETT_03("ETT03#"),
    /** 邮件附件识别异常 */
    ETT_04("ETT04#"),
    /** Tika解析异常 */
    ETT_05("ETT05#"),
    /** 邮件附件解析异常 */
    ETT_06("ETT06#"),
    /** 加密无法抽取异常 */
    ETT_07("ETT07#");

    private final String code;

    CodeEnum(String code) {
        this.code = code;
    }

    @Override
    public String getPrefix() {
        return "ETT";
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public StringBuffer append(Object target) {
        return new StringBuffer(code).append(target);
    }
}

package org.pizazz2.message;

import org.pizazz2.data.TupleObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 表达式常量
 *
 * @author xlgp2171
 * @version 2.0.200201
 */
public enum ExpressionEnum {
    /**
     * 匹配中文字符的正则表达式
     */
    CHINESE("[\\u4e00-\\u9fa5]+"),
    /**
     * 验证年龄
     */
    AGE("^[1-9]\\d?$"),
    /**
     * 中文或英文
     */
    CN_OR_EN("[\\u4e00-\\u9fa5a-zA-Z0-9]+"),
    /**
     * 匹配HTML标记的正则表达式
     */
    HTML("<(\\S*?)[^>]*>.*?</\\1>|<.*? />"),
    /**
     * 匹配首尾空白字符的正则表达式
     */
    B_E_SPACE("^\\s*|\\s*$"),
    /**
     * 匹配Email地址的正则表达式
     */
    EMAIL("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"),
    /**
     * 匹配网址URL的正则表达式:网上流传的版本功能很有限，这个基本可以满足需求
     */
    URL("[a-zA-z]+://[^\\s]*"),
    /**
     * 匹配帐号是否合法(字母开头，允许5-16字节，允许字母数字下划线)
     */
    ACCOUNT("^[a-zA-Z][a-zA-Z0-9_]{2,15}$"),
    /**
     * 匹配国内固定电话号码
     */
    TEL_PHONE("^\\d{3}-\\d{8}|\\d{4}-\\d{7,8}$"),
    /**
     * 腾讯QQ号从10000开始
     */
    QQ_NUMBER("^[1-9][0-9]{4,}$"),
    /**
     * 匹配中国邮政编码
     */
    ZIPCODE("^[1-9]\\d{5}(?!\\d)$"),
    /**
     * 匹配身份证
     */
    ID_CARD_NO("^\\d{15}|\\d{18}$"),
    /**
     * 匹配ip地址
     */
    IP_ADDRESS("\\d+\\.\\d+\\.\\d+\\.\\d+"),
    /**
     * 匹配由26个英文字母组成的字符串
     */
    LETTER("^[A-Za-z]+$"),
    /**
     * 匹配由26个英文字母的大写组成的字符串
     */
    UPPERCASE("^[A-Z]+$"),
    /**
     * 匹配由26个英文字母的小写组成的字符串
     */
    LOWERCASE("^[a-z]+$"),
    /**
     * 匹配由数字和26个英文字母组成的字符串
     */
    LETTER_DIGITAL("^[A-Za-z0-9]+$"),
    /**
     * 验证用资金帐户编号非负整数，不包括0
	 * <li/>^[1-9]+\d*$
     */
    ACCOUNT_ID("^[1-9]+\\d*$"),
    /**
     * 验证密码由大小写字母、数字和符号的8-20位组成
     */
    PWD("^(?![A-Za-z0-9]+$)(?![a-z0-9\\W]+$)(?![A-Za-z\\W]+$)(?![A-Z0-9\\W]+$)[a-zA-Z0-9\\W]{8,20}$"),
    /**
     * 验证金额
	 * <li/>^\d+(\.\d+)?$
     */
    MONEY("^[1-9]+\\d*(\\.\\d+)?$"),
    /**
     * 非空字符
     */
    NULL("^$"),
    /**
     * 验证手机号码
     */
    PHONE("^(13|15)\\d{9}$"),
    /**
     * 匹配包名称
     */
    PACKAGE_NAME("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);"),
    /**
     * 匹配类名称
     */
    CLASS_NAME("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)[\\s<\\{]+");

    static final ConcurrentMap<ExpressionEnum, Pattern> CACHE = new ConcurrentHashMap<>();

    private final String value;

    private ExpressionEnum(String value) {
        this.value = value;
    }

    public String getExpression() {
        return value;
    }

    public static Pattern getPattern(ExpressionEnum target) {
        if (!CACHE.containsKey(target)) {
            CACHE.put(target, Pattern.compile(target.getExpression()));
        }
        return CACHE.get(target);
    }
}

package org.pizazz.common.ref;

/**
 * 表达式常量
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class ExpressionConstant {
	/** 匹配中文字符的正则表达式 */
	public static final String CHINESE = "[\\u4e00-\\u9fa5]";
	/** 匹配双字节字符(包括汉字在内) */
	public static final String DOUBLE_CHINESE = "[^\\x00-\\xff]";
	/** 验证年龄 */
	public static final String AGE = "^[1-9]\\d?$";
	/** 中文或英文 */
	public static final String CNOREN = "^([\\u4e00-\\u9fa5]{2,})$|^([a-zA-Z0-9]{4,})$";
	/** 匹配HTML标记的正则表达式 */
	public static final String HTML = "<(\\S*?)[^>]*>.*?</\\1>|<.*? />";
	/** 匹配首尾空白字符的正则表达式 */
	public static final String BESPACE = "^\\s*|\\s*$";
	/** 匹配Email地址的正则表达式 */
	public static final String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
	/** 匹配网址URL的正则表达式:网上流传的版本功能很有限，这个基本可以满足需求 */
	public static final String URL = "[a-zA-z]+://[^\\s]*";
	/** 匹配帐号是否合法(字母开头，允许5-16字节，允许字母数字下划线) */
	public static final String ACCOUNT = "^[a-zA-Z][a-zA-Z0-9_]{2,15}$";
	/** 匹配国内固定电话号码 */
	public static final String TELPHONE = "^\\d{3}-\\d{8}|\\d{4}-\\d{7,8}$";
	/** 腾讯QQ号从10000开始 */
	public static final String QQNUMBER = "^[1-9][0-9]{4,}$";
	/** 匹配中国邮政编码 */
	public static final String ZIPCODE = "^[1-9]\\d{5}(?!\\d)$";
	/** 匹配身份证 */
	public static final String IDCARDNO = "^\\d{15}|\\d{18}$";
	/** 匹配ip地址 */
	public static final String IPADDRESS = "\\d+\\.\\d+\\.\\d+\\.\\d+";
	/** 匹配由26个英文字母组成的字符串 */
	public static final String LETTER = "^[A-Za-z]+$";
	/** 匹配由26个英文字母的大写组成的字符串 */
	public static final String UPPERCASE = "^[A-Z]+$";
	/** 匹配由26个英文字母的小写组成的字符串 */
	public static final String LOWERCASE = "^[a-z]+$";
	/** 匹配密码的字符串 */
	public static final String SIXNUM = "^\\d{6}$";
	/** 匹配由数字和26个英文字母组成的字符串 */
	public static final String LETTER_DIGITAL = "^[A-Za-z0-9]+$";
	/** 验证用资金帐户编号非负整数，不包括0 */ // ^[1-9]+\d*$
	public static final String ACCOUNTID = "^[1-9]+\\d*$";
	/** 验证密码 6个数字 */
	public static final String PWD = "^\\d{6}$";
	/** 验证金额 */ // ^\d+(\.\d+)?$
	public static final String MONEY = "^[1-9]+\\d*(\\.\\d+)?$";
	/** 非空字符 */
	public static final String NULL = "^$";
	/** 验证手机号码 */
	public static final String PHONE = "^(13|15)\\d{9}$";
	/** 验证18位的身份证号码 */
	public static final String CUSTOMERID = "^\\d{18}$";
	/** 匹配包名称 */
	public static final String PACKAGE_NAME = "package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);";
	/** 匹配类名称 */
	public static final String CLASS_NAME = "class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+";
}

package org.pizazz2.kafka.exception;

import org.pizazz2.message.ref.IMessageCode;

/**
 * 异常消息枚举
 *
 * @author xlgp2171
 * @version 2.0.210301
 */
public enum CodeEnum implements IMessageCode {
	/**
	 * 默认
	 */
    KFK_0000("KFK0000#"),
    /**
     * 不支持的订阅模式
     */
    KFK_0001("KFK0001#"),
    /**
     * 缺少消费模式assign配置
     */
    KFK_0002("KFK0002#"),
    /**
     * 消费模式assign配置参数错误
     */
    KFK_0003("KFK0003#"),
    /**
     * 缺少消费模式topic pattern配置
     */
    KFK_0004("KFK0004#"),
    /**
     * 缺少消费模式topic配置
     */
    KFK_0005("KFK0005#"),
    /**
     * 消费数据同步提交异常
     */
    KFK_0006("KFK0006#"),
    /**
     * 消费模式参数异常
     */
    KFK_0007("KFK0007#"),
    /**
     * 忽略模式参数异常
     */
    KFK_0008("KFK0008#"),
    /**
     * 数据接口空值
     */
    KFK_0009("KFK0009#"),
    /**
     * 订阅数据异常
     */
    KFK_0010("KFK0010#"),
    /**
     * 生产模式参数异常
     */
    KFK_0011("KFK0011#"),
    /**
     * 发送数据异常
     */
    KFK_0012("KFK0012#"),
    /**
     * 发送数据事务异常
     */
    KFK_0013("KFK0013#"),
    /**
     * 节点信息获取异常
     */
    KFK_0014("KFK0014#");

    private final String code;

    CodeEnum(String code) {
        this.code = code;
    }

    @Override
    public String getPrefix() {
        return "KFK";
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

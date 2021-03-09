package org.pizazz2.tool.ref;

/**
 * 容器状态
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public enum ContainerStatusEnum {
    /**
     * 销毁异常
     */
    DESTROY_ERROR(-1),

    /**
     * 超时异常
     */
    DESTROY_TIMEOUT(-2),

    /**
     * 线程异常
     */
    THREAD_ERROR(-3),
    /**
     * 操作完成
     */
    DESTROYED(0);

    private final int status;

    private ContainerStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

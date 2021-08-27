package org.pizazz2.tool.ref;

import org.pizazz2.common.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量数据对象
 *
 * @author xlgp2171
 * @version 2.0.210827
 *
 * @param <T> 处理类
 */
public class BatchedData<T extends IData> {
    private final int capacity;
    private final Object lock = new Object();
    private long bytes;
    private List<T> data;

    /**
     *
     * @param capacity 初始化容量
     */
    public BatchedData(int capacity) {
        this.capacity = capacity;
        reset();
    }

    public void add(T data) {
        synchronized (lock) {
            this.data.add(data);
            bytes += data.length();
        }
    }

    /**
     * 数据总数
     *
     * @return 批次数据总数
     */
    public int total() {
        return data.size();
    }

    /**
     * 数据总大小
     *
     * @return 批次数据总大小
     */
    public long bytes() {
        return bytes;
    }

    public List<T> reset() {
        List<T> tmp = data;

        synchronized (lock) {
            data = new ArrayList<>(capacity);
            bytes = NumberUtils.ZERO.longValue();
        }
        return tmp;
    }
}

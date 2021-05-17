package org.pizazz2.tool.ref;

import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.data.LinkedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据对象
 *
 * @author xlgp2171
 * @version 2.0.2105012
 */
public class BatchedData<T extends LinkedObject<byte[]>> {
    private final int capacity;
    private final Object lock = new Object();
    private long bytes = 0;
    private List<T> data;

    /**
     *
     * @param capacity 初始化容量
     */
    public BatchedData(int capacity) {
        this.capacity = capacity;
        data = new ArrayList<>(Math.max(capacity, 10));
    }

    public void add(T data) {
        synchronized (lock) {
            this.data.add(data);

            if (!ArrayUtils.isEmpty(data.getData())) {
                bytes += data.getData().length;
            }
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
            data = new ArrayList<>(Math.max(capacity, 10));
            bytes = NumberUtils.ZERO.longValue();
        }
        return tmp;
    }
}

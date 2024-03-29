package org.pizazz2.data;

import org.pizazz2.IObject;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.ValidateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * 链式对象
 *
 * @author xlgp2171
 * @version 2.1.220702
 *
 * @param <T> 元数据
 */
public class LinkedObject<T> implements IObject {
    private String id;
    /**
     * 对象名称
     */
    private final String name;
    /**
     * 对象来源
     */
    private final String source;
    /**
     * 子对象
     */
    private final Collection<? extends LinkedObject<T>> children;
    /**
     * 对象类型
     */
    private String classification = StringUtils.EMPTY;
    /**
     * 对象元数据
     */
    private T data;

    public LinkedObject(String id, String name, String source, T data) throws ValidateException {
        this(id, name, source);
        setData(data);
    }

    public LinkedObject(String id, String name, String source) throws ValidateException {
        this(id, name, source, new LinkedList<>());
    }

    public LinkedObject(String id, String name, String source, Collection<? extends LinkedObject<T>> children)
            throws ValidateException {
        this.id = id;
        this.name = StringUtils.nullToEmpty(name);
        this.source = StringUtils.nullToEmpty(source);
        this.children = children;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isEmpty() {
        return data != null && (children == null || children.isEmpty());
    }

    @Override
    public void set(String key, Object target) {
        throw new UnsupportedOperationException("linkedObject.set");
    }

    @Override
    public void reset() {
        setData(null);

        if (children != null) {
            children.clear();
        }
    }

    @Override
    public Object get(String key, Object defValue) {
        throw new UnsupportedOperationException("linkedObject.get");
    }

    public boolean processed() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public Collection<? extends LinkedObject<T>> getChildren() {
        return children;
    }

    public String getClassification() {
        return classification;
    }

    public LinkedObject<T> setClassification(String classification) {
        if (!processed()) {
            this.classification = classification;
        }
        return this;
    }

    public T getData() {
        return data;
    }

    public LinkedObject<T> setData(T data) {
        if (!processed()) {
            this.data = data;
        }
        return this;
    }

    @Override
    public IObject copy() {
        Collection<? extends LinkedObject<T>> tmp = children == null ? null : new ArrayList<>(children);
        return new LinkedObject<T>(id, name, source, tmp).setData(data).setClassification(classification);
    }

    @Override
    public String toString() {
        return id + "#" + name;
    }
}

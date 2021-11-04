package org.pizazz2.data;

import org.pizazz2.PizContext;
import org.pizazz2.IObject;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用对象
 *
 * @author xlgp2171
 * @version 2.1.211103
 *
 * @see IObject
 */
public class TupleObject extends LinkedHashMap<String, Object> implements IObject {
    private static final long serialVersionUID = 2522985541256446901L;
    private String id;

    public TupleObject() {
        super();
    }

    public TupleObject(int size) {
        super(size);
    }

    public TupleObject(Map<String, ?> target) {
        super(target);
    }

    public TupleObject append(String key, Object value) {
        put(key, value);
        return this;
    }

    public TupleObject append(Map<String, ?> value) {
        if (value != null) {
            putAll(value);
        }
        return this;
    }

    public Map<String, Object> asMap() {
        return this;
    }

    @Override
    public String getId() {
        // 默认的ID为:piz@[UUID]
        if (id == null) {
            synchronized (this) {
                if (id == null) {
                    id = PizContext.NAMING_SHORT + "@" + SystemUtils.newUUID();
                }
            }
        }
        return id;
    }

    @Override
    public void set(String key, Object target) {
        if (target instanceof TupleObject) {
            super.putAll((TupleObject) target);
        }
    }

    @Override
    public void reset() {
        super.clear();
    }

    @Override
    public IObject copy() {
        return clone();
    }

    @Override
    public Object get(String key, Object defValue) {
        if (!super.containsKey(key)) {
            return defValue;
        }
        return super.get(key);
    }

    @Override
    public TupleObject clone() throws ValidateException, IllegalException {
        TupleObject tmp = ClassUtils.cast(super.clone(), TupleObject.class);
        tmp.id = this.id;
        return tmp;
    }
}

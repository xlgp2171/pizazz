package org.pizazz2.helper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.pizazz2.common.*;
import org.pizazz2.common.ref.IKryoConfig;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;

/**
 * 通用对象工具
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public class TupleObjectHelper {

    public static TupleObject emptyObject() {
        return TupleObjectHelper.newObject(NumberUtils.ONE.intValue());
    }

    public static TupleObject newObject() {
        return new TupleObject();
    }

    public static TupleObject newObject(int size) {
        return new TupleObject(size);
    }

    public static TupleObject newObject(String key, Object value) {
        return TupleObjectHelper.newObject().append(key, value);
    }

    public static TupleObject newObject(Map<String, ?> map) {
        return new TupleObject(map);
    }

    public static boolean isEmpty(TupleObject target) {
        return target == null || target.isEmpty();
    }

    /**
     * 保证目标对象非空值
     * @param target 目标对象
     * @return 有效非空的TupleObject对象
     */
    public static TupleObject nullToEmpty(TupleObject target) {
        return target == null ? TupleObjectHelper.emptyObject() : target;
    }

    public static byte[] serialize(TupleObject target) throws ValidateException, IllegalException {
        return SerializationUtils.serialize(target, IKryoConfig.EMPTY);
    }

    public static TupleObject deserialize(byte[] target) throws ValidateException, IllegalException {
        return SerializationUtils.deserialize(target, TupleObject.class, IKryoConfig.EMPTY);
    }

    @SuppressWarnings("unchecked")
    public static TupleObject toObject(Map<String, ?> target) throws ValidateException {
        ValidateUtils.notNull("toObject", target);
        TupleObject tmp = TupleObjectHelper.newObject(target.size());

        for (Map.Entry<String, ?> item : target.entrySet()) {
            if (item.getValue() instanceof Map) {
                tmp.put(item.getKey(), TupleObjectHelper.toObject((Map<String, ?>) item.getValue()));
            } else {
                tmp.put(item.getKey(), item.getValue());
            }
        }
        return tmp;
    }

    /**
     * 将JSON转换为通用对象<br>
     * 嵌套内容不会被转换
     * @param jsonString 规范的JSON
     * @return 通用对象
     * @throws IllegalException JSON格式异常
     */
    public static TupleObject toObject(String jsonString) throws IllegalException {
        if (!StringUtils.isEmpty(jsonString) && !JSONUtils.EMPTY_JSON.equals(jsonString)) {
            return JSONUtils.fromJSON(jsonString, TupleObject.class);
        }
        return TupleObjectHelper.emptyObject();
    }

    public static TupleObject merge(TupleObject left, TupleObject right) {
        if (left == null && right == null) {
            return TupleObjectHelper.emptyObject();
        } else if (left == null) {
            return right;
        } else if (right == null) {
            return left;
        } else {
            TupleObject tmp = TupleObjectHelper.newObject(left.size() + right.size());
            tmp.putAll(left);
            tmp.putAll(right);
            return tmp;
        }
    }

    public static String getString(TupleObject target, String key, String defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        return StringUtils.of(target.get(key));
    }

    public static String getNestedString(TupleObject target, String defValue, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return defValue;
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getString(target, keys[0], defValue);
        }
        String[] tmp = new String[keys.length - 1];
        System.arraycopy(keys, 0, tmp, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getString(target, keys[keys.length - 1], defValue);
    }

    public static String[] getStringArray(TupleObject target, String key, String regex, String[] defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        Object tmp = target.get(key);

        if (tmp == null) {
            return defValue;
        } else if (tmp instanceof String[]) {
            try {
                return ClassUtils.cast(tmp, String[].class);
            } catch (ValidateException | IllegalException e) {
                return defValue;
            }
        }
        return StringUtils.of(tmp).split(regex);
    }

    public static int getInt(TupleObject target, String key, int defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        return NumberUtils.toInt(StringUtils.of(target.get(key)), defValue);
    }

    public static int getNestedInt(TupleObject target, int defValue, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return defValue;
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getInt(target, keys[0], defValue);
        }
        String[] tmp = Arrays.copyOfRange(keys, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getInt(target, keys[keys.length - 1], defValue);
    }

    public static long getLong(TupleObject target, String key, long defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        return NumberUtils.toLong(StringUtils.of(target.get(key)), defValue);
    }

    public static long getNestedLong(TupleObject target, long defValue, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return defValue;
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getLong(target, keys[0], defValue);
        }
        String[] tmp = Arrays.copyOfRange(keys, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getLong(target, keys[keys.length - 1], defValue);
    }

    public static double getDouble(TupleObject target, String key, double defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        return NumberUtils.toDouble(StringUtils.of(target.get(key)), defValue);
    }

    public static double getNestedDouble(TupleObject target, double defValue, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return defValue;
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getDouble(target, keys[0], defValue);
        }
        String[] tmp = Arrays.copyOfRange(keys, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getDouble(target, keys[keys.length - 1], defValue);
    }

    public static boolean getBoolean(TupleObject target, String key, boolean defValue) {
        if (target == null || !target.containsKey(key)) {
            return defValue;
        }
        return Boolean.parseBoolean(StringUtils.of(target.get(key)));
    }

    public static boolean getNestedBoolean(TupleObject target, boolean defValue, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return defValue;
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getBoolean(target, keys[0], defValue);
        }
        String[] tmp = Arrays.copyOfRange(keys, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getBoolean(target, keys[keys.length - 1], defValue);
    }

    @SuppressWarnings("unchecked")
    public static TupleObject getTupleObject(TupleObject target, String key) {
        if (target == null || !target.containsKey(key)) {
            return TupleObjectHelper.emptyObject();
        }
        Object item = target.get(key);
        try {
            return ClassUtils.cast(item, TupleObject.class);
        } catch (ValidateException | IllegalException e1) {
            TupleObject tmp;
            try {
                // 若是只实现了Map接口，则将对象中类型重置为TupleObject
                tmp = TupleObjectHelper.newObject(ClassUtils.cast(item, Map.class));
                target.put(key, tmp);
            } catch (ValidateException | IllegalException e2) {
                tmp = TupleObjectHelper.emptyObject();
            }
            return tmp;
        }
    }

    public static TupleObject getNestedTupleObject(TupleObject target, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return emptyObject();
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getTupleObject(target, keys[0]);
        }
        for (String key : keys) {
            target = TupleObjectHelper.getTupleObject(target, key);
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(TupleObject target, String key) throws  ValidateException, IllegalException {
        if (target == null || !target.containsKey(key)) {
            return CollectionUtils.emptyList();
        }
        return ClassUtils.cast(target.get(key), List.class);
    }

    /**
     * 按照key值顺序获取深层嵌套的List<br>
     * 最后一个key采用getList方法取值
     *
     * @param target 获取目标
     * @param keys key值顺序
     * @return List集合
     */
    public static List<Object> getNestedList(TupleObject target, String... keys) {
        if (target == null || ArrayUtils.isEmpty(keys)) {
            return CollectionUtils.emptyList();
        }
        if (keys.length == NumberUtils.ONE.intValue()) {
            return TupleObjectHelper.getList(target, keys[0]);
        }
        String[] tmp = Arrays.copyOfRange(keys, 0, keys.length - 1);
        target = TupleObjectHelper.getNestedTupleObject(target, tmp);
        return TupleObjectHelper.getList(target, keys[keys.length - 1]);
    }

    public static TupleObject copy(TupleObject target, String... keys) {
        if (target == null) {
            return TupleObjectHelper.emptyObject();
        } else if (ArrayUtils.isEmpty(keys)) {
            return target.clone();
        }
        TupleObject tmp = TupleObjectHelper.newObject(keys.length);

        for (String item : keys) {
            if (target.containsKey(item)) {
                tmp.put(item, target.get(item));
            }
        }
        return tmp;
    }
}

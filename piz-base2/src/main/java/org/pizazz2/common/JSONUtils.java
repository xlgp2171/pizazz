package org.pizazz2.common;

import com.alibaba.fastjson.JSONException;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import java.util.List;

/**
 * 对象JSON处理
 * <li/>使用fastjson组件
 *
 * @author xlgp2171
 * @version 2.1.210917
 */
public class JSONUtils {
    /** 空的JSON字符串 */
    public final static String EMPTY_JSON;

    static {
        EMPTY_JSON = JSONUtils.toJSON(TupleObjectHelper.emptyObject());
    }

    public static String toJSON(Object target) throws IllegalException {
        return JSONUtils.toJSON(target, false);
    }

    public static String toJSON(Object target, boolean prettyFormat) throws IllegalException {
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.toJSONString(target, prettyFormat);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new IllegalException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }

    public static <T> T fromJSON(String target, Class<T> type) throws IllegalException {
        // return fromJSON(target, type, null);
        // 默认采用fastjson，支持大对象处理
        try {

            return com.alibaba.fastjson.JSONObject.parseObject(target, type);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new IllegalException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }

    public static <T> List<T> fromJSONArray(String target, Class<T> type) throws IllegalException {
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.parseArray(target, type);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new IllegalException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }
}

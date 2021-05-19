package org.pizazz2.common;

import com.alibaba.fastjson.JSONException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import java.util.List;

/**
 * 对象JSON处理
 * <li/>使用jackson组件
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class JSONUtils {
    /** 空的JSON字符串 */
    public final static String EMPTY_JSON;

    static {
        EMPTY_JSON = JSONUtils.toJSON(TupleObjectHelper.emptyObject());
    }

    /*
    public static String toJSON(Object target, IJacksonConfig config) throws UtilityException {
        if (target == null) {
            return "{}";
        } else if (config == null) {
            config = IJacksonConfig.EMPTY;
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
        config.set(mapper);
        try {
            return mapper.writeValueAsString(target);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }

    public static <T> T fromJSON(String target, Class<T> type, IJacksonConfig config) throws ValidateException, UtilityException {
        ValidateUtils.notNull("fromJSON", target, type);

        if (config == null) {
            config = IJacksonConfig.EMPTY;
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper(config.factory());
        config.set(mapper);
        try {
            return mapper.readValue(target, type);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JACKSON.PROCESS", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }
     */

    public static String toJSON(Object target) throws ValidateException {
        return JSONUtils.toJSON(target, false);
    }

    public static String toJSON(Object target, boolean prettyFormat) throws ValidateException {
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.toJSONString(target, prettyFormat);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new ValidateException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }

    public static <T> T fromJSON(String target, Class<T> type) throws ValidateException {
        // return fromJSON(target, type, null);
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.parseObject(target, type);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new ValidateException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }

    public static <T> List<T> fromJSONArray(String target, Class<T> type) {
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.parseArray(target, type);
        } catch (JSONException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.FASTJSON.PROCESS", e.getMessage());
            throw new ValidateException(BasicCodeEnum.MSG_0013, msg, e);
        }
    }
}

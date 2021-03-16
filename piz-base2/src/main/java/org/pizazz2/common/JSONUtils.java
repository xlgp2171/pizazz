package org.pizazz2.common;

import com.alibaba.fastjson.JSONException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 对象JSON处理
 * <li/>使用jackson组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class JSONUtils {

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
        // return toJSON(target, null);
        // 默认采用fastjson，支持大对象处理
        try {
            return com.alibaba.fastjson.JSONObject.toJSONString(target);
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
}

package org.pizazz2.common;

import org.pizazz2.IObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ExpressionEnum;
import org.pizazz2.message.TypeEnum;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;

/**
 * 验证工具
 *
 * @author xlgp2171
 * @version 2.1.210914
 */
public class ValidateUtils {
    public static void verifyExpression(ExpressionEnum expression, String target) throws ValidateException {
        ValidateUtils.notNull("verifyExpression", expression, target);

        if (!ExpressionEnum.getPattern(expression).matcher(target).matches()) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.EXPRESSION.MATCHES", target,
					expression.name());
            throw new ValidateException(BasicCodeEnum.MSG_0026, msg);
        }
    }

    public static void notEmpty(String method, IObject target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, IObject target, int seq) {
        if (target == null || target.isEmpty()) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void notEmpty(String method, Collection<?> target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, Collection<?> target, int seq) throws ValidateException {
        if (CollectionUtils.isEmpty(target)) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void notEmpty(String method, byte[] target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, byte[] target, int seq) throws ValidateException {
        if (ArrayUtils.isEmpty(target)) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void notEmpty(String method, String[] target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, String[] target, int seq) throws ValidateException {
        if (ArrayUtils.isEmpty(target)) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void notEmpty(String method, String target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, String target, int seq) throws ValidateException {
        if (StringUtils.isTrimEmpty(target)) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void notNull(String method, Object... arguments) throws ValidateException {
        String msg = null;

        if (ArrayUtils.isEmpty(arguments)) {
            msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, 1);
        }
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, i + 1);
            }
        }
        if (msg != null) {
            throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
        }
    }

    public static void isTrue(String method, boolean target) throws ValidateException {
        if (!target) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.BOOLEAN", method, "true");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void isFalse(String method, boolean target) throws ValidateException {
        if (target) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.BOOLEAN", method, "false");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void equals(String method, Object[] left, Object[] right) throws ValidateException {
        ValidateUtils.notNull("equals", left, right);

        if (left.length != right.length) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "length");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
        for (int i = 0; i < left.length; i++) {
            if (!Objects.equals(left[i], right[i])) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
                throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
            }
        }
    }

    public static void equals(String method, byte[] left, byte[] right) throws ValidateException {
        ValidateUtils.notNull("equals", left, right);

        if (left.length != right.length) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "length");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
                throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
            }
        }
    }

    public static void equals(String method, String left, String right) throws ValidateException {
        ValidateUtils.notNull("equals", left, right);

        if (!left.equals(right)) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void equals(String method, long left, long right) throws ValidateException {
        ValidateUtils.notNull("assertEquals", left, right);

        if (left != right) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void sameLength(String method, int index, String target, int length) throws ValidateException {
        if (target == null || target.length() != length) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void sameLength(String method, int index, byte[] target, int length) throws ValidateException {
        if (target == null || target.length != length) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    public static void limit(String method, int index, Number target, Number min, Number max) throws ValidateException {
        ValidateUtils.notNull("limit", 1, 1, target);
        boolean result = true;
        String tmp = "";

        if (min != null && target.doubleValue() < min.doubleValue()) {
            tmp = ">=" + min;
            result = false;
        }
        if (max != null && target.doubleValue() > max.doubleValue()) {
            tmp = "<=" + max;
            result = false;
        }
        if (!result) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, tmp);
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    /**
     * 验证应为合成类
     * @param method 验证目标方法
     * @param target 验证目标
     * @throws ValidateException 目标不为合成类
     */
    public static void isSyntheticClass(String method, Object target) throws ValidateException {
        if (target == null || !target.getClass().isSynthetic()) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.MUST", method, "Synthetic Class");
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }

    /**
     * 验证应为日期格式字符串<br>
     * 默认验证格式{@link DateUtils#DEFAULT_FORMAT}
     * @param target 验证目标
     * @throws ValidateException 目标不为日期格式
     */
    public static void isDateTimeFormat(String target) throws ValidateException {
        ValidateUtils.isDateTimeFormat(target, DateUtils.DEFAULT_FORMAT);
    }

    /**
     * 验证应为日期格式字符串
     * @param target 验证目标
     * @param pattern 日期格式组
     * @throws ValidateException 目标不为日期格式
     */
    public static void isDateTimeFormat(String target, String... pattern) throws ValidateException {
        ValidateUtils.notEmpty("isDate", pattern, 2);
        boolean ignore = false;
        ValidateException tmp = new ValidateException(StringUtils.EMPTY, null);

        for (String item : pattern) {
            DateTimeFormatter formatter;
            try {
                formatter = DateTimeFormatter.ofPattern(item);
            } catch (NullPointerException | IllegalArgumentException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.MUST", item, "Date Pattern");
                tmp = new ValidateException(BasicCodeEnum.MSG_0028, msg);
                continue;
            }
            try {
                DateUtils.parse(target, formatter);
                ignore = true;
            } catch (ValidateException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.MUST", item, "Date Format");
                tmp = new ValidateException(BasicCodeEnum.MSG_0017, msg);
            }
        }
        if (!ignore) {
            throw tmp;
        }
    }
}

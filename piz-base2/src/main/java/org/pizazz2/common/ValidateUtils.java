package org.pizazz2.common;

import org.pizazz2.IObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ExpressionEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.message.ref.IMessageCode;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 验证工具
 *
 * @author xlgp2171
 * @version 2.2.230315
 */
public class ValidateUtils {
    static void throwException(IMessageCode code, Supplier<String> message) {
        throw new ValidateException(code, message == null ? StringUtils.EMPTY : message.get());
    }
    // ----------

    public static void verifyExpression(ExpressionEnum expression, String target) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.EXPRESSION.MATCHES", target, expression.name());
        ValidateUtils.verifyExpression(BasicCodeEnum.MSG_0026, expression, target, () -> msg);
    }

    public static void verifyExpression(IMessageCode code, ExpressionEnum expression, String target,
                                        Supplier<String> message) throws ValidateException {
        ValidateUtils.notNull("verifyExpression", expression, target);

        if (!ExpressionEnum.getPattern(expression).matcher(target).matches()) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void notEmpty(IMessageCode code, IObject target) {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(IMessageCode code, IObject target, Supplier<String> message) {
        if (target == null || target.isEmpty()) {
            ValidateUtils.throwException(code, message);
        }
    }

    public static void notEmpty(String method, IObject target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, NumberUtils.ONE.intValue());
    }

    public static void notEmpty(String method, IObject target, int seq) {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }
    // ----------

    public static void notEmpty(IMessageCode code, Collection<?> target) throws ValidateException {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(IMessageCode code, Collection<?> target, Supplier<String> message)
            throws ValidateException {
        if (CollectionUtils.isEmpty(target)) {
            ValidateUtils.throwException(code, message);
        }
    }

    public static void notEmpty(String method, Collection<?> target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, NumberUtils.ONE.intValue());
    }

    public static void notEmpty(String method, Collection<?> target, int seq) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }
    // ----------

    public static void notEmpty(IMessageCode code, Map<?, ?> target) throws ValidateException {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(String method, Map<?, ?> target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, NumberUtils.ONE.intValue());
    }

    public static void notEmpty(String method, Map<?, ?> target, int seq) {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }

    public static void notEmpty(IMessageCode code, Map<?, ?> target, Supplier<String> message) throws ValidateException {
        if (CollectionUtils.isEmpty(target)) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void notEmpty(IMessageCode code, byte[] target) throws ValidateException {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(String method, byte[] target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, NumberUtils.ONE.intValue());
    }

    public static void notEmpty(String method, byte[] target, int seq) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }

    public static void notEmpty(IMessageCode code, byte[] target, Supplier<String> message) throws ValidateException {
        if (ArrayUtils.isEmpty(target)) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void notEmpty(IMessageCode code, Object[] target) throws ValidateException {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(String method, Object[] target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, NumberUtils.ONE.intValue());
    }

    public static void notEmpty(String method, Object[] target, int seq) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }

    public static void notEmpty(IMessageCode code, Object[] target, Supplier<String> message) throws ValidateException {
        if (ArrayUtils.isEmpty(target)) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void notEmpty(IMessageCode code, String target) throws ValidateException {
        ValidateUtils.notEmpty(code, target, null);
    }

    public static void notEmpty(String method, String target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, String target, int seq) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, seq);
        ValidateUtils.notEmpty(BasicCodeEnum.MSG_0001, target, () -> msg);
    }

    public static void notEmpty(IMessageCode code, String target, Supplier<String> message) throws ValidateException {
        if (StringUtils.isTrimEmpty(target)) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void notNull(IMessageCode code, Object... arguments) throws ValidateException {
        if (ArrayUtils.isEmpty(arguments)) {
            throw new ValidateException(code, StringUtils.EMPTY);
        }
        for (Object item : arguments) {
            if (item == null) {
                throw new ValidateException(code, StringUtils.EMPTY);
            }
        }
    }

    public static void notNull(String method, Object... arguments) throws ValidateException {
        String msg = null;

        if (ArrayUtils.isEmpty(arguments)) {
            msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method,
                    NumberUtils.ONE.intValue());
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
    // ----------

    public static void isTrue(IMessageCode code, boolean target) throws ValidateException {
        ValidateUtils.isTrue(code, target, null);
    }

    public static void isTrue(String method, boolean target) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.BOOLEAN", method, "true");
        ValidateUtils.isTrue(BasicCodeEnum.MSG_0005, target, () -> msg);
    }

    public static void isTrue(IMessageCode code, boolean target, Supplier<String> message) throws ValidateException {
        if (!target) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void isFalse(IMessageCode code, boolean target) throws ValidateException {
        ValidateUtils.isFalse(code, target, null);
    }

    public static void isFalse(String method, boolean target) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.BOOLEAN", method, "false");
        ValidateUtils.isFalse(BasicCodeEnum.MSG_0005, target, () -> msg);
    }

    public static void isFalse(IMessageCode code, boolean target, Supplier<String> message) throws ValidateException {
        if (target) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void equals(IMessageCode code, Object[] left, Object[] right) throws ValidateException {
        ValidateUtils.notNull(code, left, right);

        if (left.length != right.length) {
            throw new ValidateException(code, StringUtils.EMPTY);
        }
        for (int i = 0; i < left.length; i++) {
            if (!Objects.equals(left[i], right[i])) {
                throw new ValidateException(code, StringUtils.EMPTY);
            }
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
    // ----------

    public static void equals(IMessageCode code, byte[] left, byte[] right) throws ValidateException {
        ValidateUtils.notNull(code, left, right);

        if (left.length != right.length) {
            throw new ValidateException(code, StringUtils.EMPTY);
        }
        for (int i = 0; i < left.length; i++) {
            if (!Objects.equals(left[i], right[i])) {
                throw new ValidateException(code, StringUtils.EMPTY);
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
    // ---------
    public static void equals(IMessageCode code, long left, long right) throws ValidateException {
        ValidateUtils.equals(code, left, right, null);
    }

    public static void equals(String method, long left, long right) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", method, "value");
        ValidateUtils.equals(BasicCodeEnum.MSG_0005, left, right, () -> msg);
    }

    public static void equals(IMessageCode code, long left, long right, Supplier<String> message)
            throws ValidateException {
        if (left != right) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void sameLength(IMessageCode code, int index, String target, int length)
            throws ValidateException {
        ValidateUtils.sameLength(code, index, target, length, null);
    }

    public static void sameLength(String method, int index, String target, int length) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
        ValidateUtils.sameLength(BasicCodeEnum.MSG_0005, index, target, length, () -> msg);
    }

    public static void sameLength(IMessageCode code, int index, String target, int length, Supplier<String> message)
            throws ValidateException {
        if (target == null || target.length() != length) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void sameLength(IMessageCode code, int index, byte[] target, int length)
            throws ValidateException {
        ValidateUtils.sameLength(code, index, target, length, null);
    }

    public static void sameLength(String method, int index, byte[] target, int length) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
        ValidateUtils.sameLength(BasicCodeEnum.MSG_0005, index, target, length, () -> msg);
    }

    public static void sameLength(IMessageCode code, int index, byte[] target, int length, Supplier<String> message)
            throws ValidateException {
        if (target == null || target.length != length) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void sameLength(IMessageCode code, int index, Object[] target, int length)
            throws ValidateException {
        ValidateUtils.sameLength(code, index, target, length, null);
    }

    public static void sameLength(String method, int index, Object[] target, int length) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, length);
        ValidateUtils.sameLength(BasicCodeEnum.MSG_0005, index, target, length, () -> msg);
    }

    public static void sameLength(IMessageCode code, int index, Object[] target, int length, Supplier<String> message)
            throws ValidateException {
        if (target == null || target.length != length) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void limit(IMessageCode code, int index, Number target, Number min, Number max)
            throws ValidateException {
        ValidateUtils.limit(code,index, target, min, max, null);
    }

    public static void limit(String method, int index, Number target, Number min, Number max) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, StringUtils.EMPTY);
        ValidateUtils.limit(BasicCodeEnum.MSG_0005, index, target, min, max, () -> msg);
    }

    public static void limit(IMessageCode code, int index, Number target, Number min, Number max,
                             Supplier<String> message) throws ValidateException {
        boolean result = true;

        if (target != null) {
            if (min != null && target.doubleValue() < min.doubleValue()) {
                result = false;
            }
            if (max != null && target.doubleValue() > max.doubleValue()) {
                result = false;
            }
        }
        if (!result) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

    public static void isSyntheticClass(IMessageCode code, Object target) throws ValidateException {
        ValidateUtils.isSyntheticClass(code, target, null);
    }

    /**
     * 验证应为合成类
     * @param method 验证目标方法
     * @param target 验证目标
     * @throws ValidateException 目标不为合成类
     */
    public static void isSyntheticClass(String method, Object target) throws ValidateException {
        String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.MUST", method, "Synthetic Class");
        ValidateUtils.isSyntheticClass(BasicCodeEnum.MSG_0005, target, () -> msg);
    }

    public static void isSyntheticClass(IMessageCode code, Object target, Supplier<String> message)
            throws ValidateException {
        if (target == null || !target.getClass().isSynthetic()) {
            ValidateUtils.throwException(code, message);
        }
    }
    // ----------

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
        if (ArrayUtils.isEmpty(pattern)) {
            return;
        }
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

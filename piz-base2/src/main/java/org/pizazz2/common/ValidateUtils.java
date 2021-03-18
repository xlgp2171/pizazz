package org.pizazz2.common;

import org.pizazz2.IObject;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.ExpressionEnum;
import org.pizazz2.message.TypeEnum;

import java.util.Objects;

/**
 * 验证工具
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class ValidateUtils {
    public static void verifyExpression(ExpressionEnum expression, String target) throws ValidateException {
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

    public static void notEmpty(String method, byte[] target) throws ValidateException {
        ValidateUtils.notEmpty(method, target, 1);
    }

    public static void notEmpty(String method, byte[] target, int seq) throws ValidateException {
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
        if (!ArrayUtils.isEmpty(arguments)) {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == null) {
                    String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", method, i + 1);
                    throw new ValidateException(BasicCodeEnum.MSG_0001, msg);
                }
            }
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

    public static void limit(String method, int index, int target, Integer min, Integer max) throws ValidateException {
        boolean result = true;
        String tmp = "";

        if (min != null && target < min) {
            tmp = ">=" + min;
            result = false;
        }
        if (max != null && target > max) {
            tmp = "<=" + max;
            result = false;
        }
        if (!result) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.LENGTH", method, index, tmp);
            throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
        }
    }
}

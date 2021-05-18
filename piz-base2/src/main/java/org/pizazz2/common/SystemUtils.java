package org.pizazz2.common;

import org.pizazz2.ICloseable;
import org.pizazz2.common.ref.IMXBean;
import org.pizazz2.common.ref.OSTypeEnum;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import javax.management.*;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.UUID;

/**
 * 系统工具
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class SystemUtils {
    public static String getSystemProperty(String property, String defValue) {
        try {
            return System.getProperty(property, defValue);
        } catch (SecurityException e) {
            println(System.err, BasicCodeEnum.MSG_0002.append("getProperty'").append(property).append("':").append(e.getMessage()));
            return defValue;
        }
    }

    /**
     * 获取当前操作系统
     * @param osType 操作系统字符串（获取os.name）
     * @return 操作系统类型
     * @throws ValidateException 验证异常
     */
    public static OSTypeEnum getOSType(String osType) throws ValidateException {
        if (!StringUtils.isTrimEmpty(osType)) {
            OSTypeEnum[] tmp = OSTypeEnum.values();

            for (OSTypeEnum item : tmp) {
                if (osType.startsWith(item.toString())) {
                    return item;
                }
            }
        }
        throw new ValidateException(BasicCodeEnum.MSG_0023, osType);
    }

    public static void println(PrintStream target, StringBuffer message) {
        if (target != null) {
            target.println(message);
        } else {
            System.err.println(message);
        }
    }

    /**
     * 获取VM名称
     * @return VM名称（进程号@计算机名）
     */
    public static String getVMName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    public static String getComputerName(RuntimeMXBean runtime) throws ValidateException {
        ValidateUtils.notNull("getComputerName", runtime);
        String name = runtime.getName();
        int index = name.indexOf("@");
        return index != -1 ? name.substring(index + 1) : name;
    }

    /**
     * 获取计算机名
     * @return 计算机名
     * @throws ValidateException 验证异常
     */
    public static String getComputerName() throws ValidateException {
        return SystemUtils.getComputerName(ManagementFactory.getRuntimeMXBean());
    }

    /**
     * 注册JMX<br>
     * 实现子接口必须以MXBean结尾才有效
     *
     * @param target MXBean实现类
     * @return 服务注册对象实例
     *
     * @throws ValidateException 验证异常
     * @throws UtilityException 创建MXBean异常
     */
    public static ObjectInstance registerMBean(IMXBean target) throws ValidateException, UtilityException {
        ValidateUtils.notNull("registerMBean", target);
        // org.pizazz.package:type=Name
        String objectId = ReflectUtils.getPackageName(target.getClass()) + ":type=" + target.getClass().getSimpleName();
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName name;
        try {
            name = new ObjectName(objectId);
        } catch (MalformedObjectNameException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MBEAN.NAME", objectId, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0005, msg, e);
        }
        try {
            return server.registerMBean(target, name);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MBEAN.REGISTRY", target.getClass().getName(), name, e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0011, msg, e);
        }
    }

    public static int getProcessId(RuntimeMXBean runtime) {
        String name = runtime.getName();
        int index = name.indexOf("@");
        return NumberUtils.toInt(index != -1 ? name.substring(0, index) : name, -1);
    }

    /**
     * 获取当前进程号
     * @return 进程号
     */
    public static int getProcessId() {
        return SystemUtils.getProcessId(ManagementFactory.getRuntimeMXBean());
    }

    /**
     * 增加关闭狗子
     * @param closeable 实现关闭接口类
     * @param timeout 超时时间
     * @return 关闭狗子的线程
     */
    public static Thread addShutdownHook(ICloseable closeable, Duration timeout) {
        Thread tmp = new Thread(() -> SystemUtils.destroy(closeable, timeout));
        Runtime.getRuntime().addShutdownHook(tmp);
        return tmp;
    }

    public static String createId(Object target) {
        if (target == null) {
            return "null";
        }
        return target.getClass().getName() + "@" + System.identityHashCode(target);
    }

    /**
     * 获取UUID
     * @return UUID
     */
    public static String newUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取简单化的UUID，除开“-”
     * @return 简单化的UUID
     */
    public static String newUUIDSimple() {
        return SystemUtils.newUUID().replaceAll("-", "");
    }

    /**
     * 安全关闭
     * @param target 实现关闭接口的实现类
     * @param timeout 关闭超时时间
     */
    public static void destroy(ICloseable target, Duration timeout) {
        if (target != null) {
            if (timeout != null) {
                try {
                    target.destroy(timeout);
                } catch (Exception e) {
                    // do nothing
                }
            } else {
                SystemUtils.close(target);
            }
        }
    }

    /**
     * 安全关闭
     * @param target 实现AutoCloseable接口的类
     */
    public static void close(AutoCloseable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}

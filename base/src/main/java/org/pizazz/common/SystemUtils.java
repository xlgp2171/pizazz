package org.pizazz.common;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.common.ref.IMXBean;
import org.pizazz.common.ref.OSTypeEnum;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ErrorCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 系统工具
 * 
 * @author xlgp2171
 * @version 1.0.181220
 */
public class SystemUtils {
	/**
	 * 当前操作系统
	 */
	public static final OSTypeEnum LOCAL_OS;
	/**
	 * 当前时区
	 */
	public static final Locale LOCAL_LOCALE;
	/**
	 * 当前目录
	 */
	public static final Path LOCAL_DIR;
	/**
	 * 当前编码
	 */
	public static final Charset LOCAL_ENCODING;
	/**
	 * 当前系统目录分隔符
	 */
	public static final String FILE_SEPARATOR = getSystemProperty("file.separator", "/");
	/**
	 * 当前系统换行符
	 */
	public static final String LINE_SEPARATOR = getSystemProperty("line.separator", "\n");
	static volatile Object TEMP = null;

	static {
		LOCAL_OS = getOSType(getSystemProperty("os.name", StringUtils.EMPTY).toLowerCase());
		// 默认中文环境
		String _tmp = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".locale", "zh_CN");
		Locale _tmpL = Locale.forLanguageTag(_tmp);
		LOCAL_LOCALE = StringUtils.isTrimEmpty(_tmpL.toString()) ? Locale.forLanguageTag("zh_CN") : _tmpL;
		Path _tmpPath;
		try {
			_tmpPath = Paths.get("").toRealPath();
		} catch (SecurityException | IOException e) {
			_tmpPath = Paths.get(getSystemProperty("user.dir", StringUtils.EMPTY));
		}
		LOCAL_DIR = _tmpPath;
		// 默认UTF-8
		String _defE = getSystemProperty("file.encoding", StandardCharsets.UTF_8.name());
		_tmp = SystemUtils.getSystemProperty(Constant.NAMING_SHORT + ".encoding", _defE);
		Charset _tmpC;
		try {
			_tmpC = Charset.forName(_tmp);
		} catch (UnsupportedCharsetException e) {
			_tmpC = Charset.forName(_defE);
		}
		LOCAL_ENCODING = _tmpC;
	}

	public static String getSystemProperty(String property, String defValue) {
		try {
			return System.getProperty(property, defValue);
		} catch (SecurityException e) {
			println(System.err, new StringBuilder(BasicCodeEnum.MSG_0002.getValue()).append("getProperty'")
					.append(property).append("':").append(e.getMessage()));
			return defValue;
		}
	}

	public static OSTypeEnum getOSType(String osType) {
		if (StringUtils.isTrimEmpty(osType)) {
			throw new BaseError(ErrorCodeEnum.ERR_0001);
		}
		OSTypeEnum[] _tmp = OSTypeEnum.values();

		for (OSTypeEnum _item : _tmp) {
			if (osType.startsWith(_item.toString())) {
				return _item;
			}
		}
		throw new BaseError(ErrorCodeEnum.ERR_0001, "OSType:" + osType);
	}

	public static void println(PrintStream target, StringBuilder message) {
		if (target != null) {
			target.println(message);
		} else {
			System.err.println(message);
		}
	}

	public static String getVMName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	public static String getComputerName(RuntimeMXBean runtime) throws BaseException {
		AssertUtils.assertNotNull("getComputerName", runtime);
		String _name = runtime.getName();
		int _index = _name.indexOf("@");
		return _index != -1 ? _name.substring(_index + 1) : _name;
	}

	public static String getComputerName() throws BaseException {
		return getComputerName(ManagementFactory.getRuntimeMXBean());
	}

	/**
	 * 注册JMX<br>
	 * 实现子接口必须以MXBean结尾才有效
	 * 
	 * @param target
	 * @return
	 * @throws BaseException
	 */
	public static ObjectInstance registerMBean(IMXBean target) throws BaseException {
		AssertUtils.assertNotNull("registerMBean", target);
		// org.pizazz.package:type=Name
		String _objectId = new StringBuilder(ReflectUtils.getPackageName(target.getClass())).append(":type=")
				.append(target.getClass().getSimpleName()).toString();
		MBeanServer _server = ManagementFactory.getPlatformMBeanServer();
		ObjectName _name;
		try {
			_name = new ObjectName(_objectId);
		} catch (MalformedObjectNameException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MBEAN.NAME", _objectId, e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg, e);
		}
		try {
			return _server.registerMBean(target, _name);
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MBEAN.REGISTRY", target.getClass().getName(),
					_name, e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0011, _msg, e);
		}
	}

	public static int getProcessID(RuntimeMXBean runtime) {
		String _name = runtime.getName();
		int _index = _name.indexOf("@");
		return NumberUtils.toInt(_index != -1 ? _name.substring(0, _index) : _name, -1);
	}

	public static int getProcessID() {
		return getProcessID(ManagementFactory.getRuntimeMXBean());
	}

	public static String createID(Object target) {
		if (target == null) {
			return "null";
		}
		return target.getClass().getName() + "@" + System.identityHashCode(target);
	}

	public static String newUUID() {
		return UUID.randomUUID().toString();
	}

	public static String newUUIDSimple() {
		return newUUID().replaceAll("-", "");
	}

	public static void destroy(ICloseable target, Duration timeout) {
		if (target != null) {
			if (timeout!= null && !timeout.isNegative()) {
				try {
					target.destroy(timeout);
				} catch (BaseException e) {
				}
			} else {
				IOUtils.close(target);
			}
		}
	}

	public static synchronized void clear(Object target) {
		TEMP = target;
		target = null;
		TEMP = null;
	}
}

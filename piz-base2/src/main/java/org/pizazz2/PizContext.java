package org.pizazz2;

import org.pizazz2.common.StringUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ref.OSTypeEnum;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 全局环境
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public final class PizContext {
    /**
     * 全局名称
     */
    public static final String NAMING = "pizazz";
    /**
     * 缩写名称
     */
    public static final String NAMING_SHORT = "piz";
	/**
	 * piz版本（当前2）
	 */
	public static final byte VERSION = 2;
	/**
	 * 属性前缀
	 */
    public static final String ATTRIBUTE_PREFIX = "$";
	/**
	 * 当前框架类加载器
	 */
	public static final ClassLoader CLASS_LOADER;

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
	public static final Path LOCAL_PATH;
	/**
	 * 当前系统临时文件夹
	 */
	public static final Path TEMP_DIRECTORY;
	/**
	 * 当前编码
	 */
	public static final Charset LOCAL_ENCODING;
	/**
	 * 当前系统目录分隔符
	 */
	public static final String FILE_SEPARATOR = SystemUtils.getSystemProperty("file.separator", "/");
	/**
	 * 当前系统换行符
	 */
	public static final String LINE_SEPARATOR = SystemUtils.getSystemProperty("line.separator", "\n");

	static {
		LOCAL_OS = SystemUtils.getOSType(SystemUtils.getSystemProperty("os.name", StringUtils.EMPTY).toLowerCase());
		// 默认英文环境
		String tmp = SystemUtils.getSystemProperty(PizContext.NAMING_SHORT + ".locale", "en-US");
		Locale tmpL = Locale.forLanguageTag(tmp);
		LOCAL_LOCALE = StringUtils.isTrimEmpty(tmpL.toString()) ? Locale.forLanguageTag("zh-CN") : tmpL;
		Path tmpPath;
		try {
			tmpPath = Paths.get("").toRealPath();
		} catch (SecurityException | IOException e) {
			tmpPath = Paths.get(SystemUtils.getSystemProperty("user.dir", StringUtils.EMPTY));
		}
		LOCAL_PATH = tmpPath;
		// 临时文件夹(无权限检查)
		String tempDirectory = SystemUtils.getSystemProperty("java.io.tmpdir", StringUtils.EMPTY);
		TEMP_DIRECTORY = StringUtils.isEmpty(tempDirectory) ? LOCAL_PATH : Paths.get(tempDirectory);
		// 默认UTF-8
		String defE = SystemUtils.getSystemProperty("file.encoding", StandardCharsets.UTF_8.name());
		tmp = SystemUtils.getSystemProperty(PizContext.NAMING_SHORT + ".encoding", defE);
		Charset tmpC;
		try {
			tmpC = Charset.forName(tmp);
		} catch (UnsupportedCharsetException e) {
			tmpC = Charset.forName(defE);
		}
		LOCAL_ENCODING = tmpC;
		//
		CLASS_LOADER = PizContext.class.getClassLoader();
	}
}

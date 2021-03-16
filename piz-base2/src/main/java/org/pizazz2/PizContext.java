package org.pizazz2;

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

	static {
		CLASS_LOADER = PizContext.class.getClassLoader();
	}
}

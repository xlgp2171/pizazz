package org.pizazz2.tool;

import org.pizazz2.common.ValidateUtils;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 编译超类
 * <li/>参考com.alibaba.dubbo.common.compiler.support.AbstractCompiler
 * 
 * @author xlgp2171
 * @version 2.0.210425
 */
public abstract class AbstractCompiler {
	public Class<?> compile(String source, ClassLoader loader) throws ValidateException, ToolException {
		ValidateUtils.notNull("compile", source);
		String packageName = ClassUtils.getPackageName(source);
		String name = ClassUtils.getClassName(source);
		String classpath = StringUtils.isEmpty(packageName) ? name : packageName + "." + name;
		try {
			return ClassUtils.loadClass(classpath, loader, true);
		} catch (UtilityException e) {
			// 当类无法加载时，.class未编译
			try {
				return doCompile(classpath, source.trim(), loader);
			} catch (Throwable t) {
				String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.COMPILE", classpath,
						t.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0006, msg, t);
			}
		}
	}

	/**
	 * 实现编译
	 * @param classpath 类路径
	 * @param source 源码
	 * @param loader 类加载器
	 * @return 加载后的类实现
	 * @throws Throwable 加载异常或字节码错误
	 */
	protected abstract Class<?> doCompile(String classpath, String source, ClassLoader loader) throws Throwable;
}

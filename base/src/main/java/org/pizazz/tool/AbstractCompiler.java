package org.pizazz.tool;

import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.StringUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 编译超类<br>
 * 参考com.alibaba.dubbo.common.compiler.support.AbstractCompiler
 * 
 * @author xlgp2171
 * @version 1.0.191014
 */
public abstract class AbstractCompiler {
	public Class<?> compile(String source, ClassLoader loader) throws BaseException {
		AssertUtils.assertNotNull("compile", source);
		String _package;
		try {
			_package = ClassUtils.getPackageName(source);
		} catch (BaseException e) {
			// 无Package的类时
			_package = StringUtils.EMPTY;
		}
		String _name = ClassUtils.getClassName(source);
		String _classpath = StringUtils.isEmpty(_package) ? _name : _package + "." + _name;
		try {
			return ClassUtils.loadClass(_classpath, loader, true);
		} catch (BaseException e) {
			try {
				return doCompile(_classpath, source.trim(), loader);
			} catch (Throwable t) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "BASIC.ERR.CLASS.COMPILE", _classpath,
						t.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0006, _msg, t);
			}
		}
	}

	protected abstract Class<?> doCompile(String classpath, String source, ClassLoader loader) throws Throwable;
}

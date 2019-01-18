package org.pizazz.tool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.pizazz.ICloseable;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.CollectionUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.PathUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

public class PCompiler implements ICloseable {
	private final JavaCompiler compiler;
	/** 诊断信息 */
	private final DiagnosticCollector<JavaFileObject> collector;
	private final StandardJavaFileManager manager;
	private final Set<String> options = new LinkedHashSet<String>();
	private final List<String> paths = new LinkedList<String>();
	private final PClassLoader loader;

	public PCompiler(PClassLoader loader) throws BaseException {
		AssertUtils.assertNotNull("PCompiler", loader);
		compiler = ToolProvider.getSystemJavaCompiler();
		collector = new DiagnosticCollector<JavaFileObject>();
		manager = compiler.getStandardFileManager(collector, null, null);
		this.loader = loader;
	}

	public PCompiler setOptions(String... options) {
		// 增加参数可以跟编译依赖javac -extdirs lib;lib_sev;icelib -Xlint:unchecked
		if (!ArrayUtils.isEmpty(options)) {
			this.options.addAll(Arrays.asList(options));
		}
		return this;
	}

	public PCompiler addResource(Path dir, Path name) throws BaseException {
		AssertUtils.assertNotNull("addResource", dir, name);
		Path _source = dir.resolve(name);

		if (Files.exists(_source)) {
			PathUtils.copy(_source, loader.getDirectory().resolve(name));
		}
		return this;
	}

	public PCompiler addFile(Path path) throws BaseException {
		AssertUtils.assertNotNull("addFile", path);

		if (PathUtils.isRegularFile(path) && path.toString().endsWith(JavaFileObject.Kind.SOURCE.extension)) {
			paths.add(path.toString());
		} else {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path);
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
		}
		return this;
	}

	public PClassLoader compile() throws BaseException {
		options.addAll(Arrays.asList("-d", loader.getDirectory().toString(), "-Xlint:unchecked"));
		Iterable<? extends JavaFileObject> _files = manager.getJavaFileObjectsFromStrings(paths);
		JavaCompiler.CompilationTask task = compiler.getTask(null, manager, collector, options, null, _files);
		Boolean _result = task.call();

		if (_result == null || !_result.booleanValue()) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.COMPILE",
					CollectionUtils.toString(options), CollectionUtils.toString(paths),
					CollectionUtils.toString(collector.getDiagnostics()));
			throw new BaseException(BasicCodeEnum.MSG_0022, _msg);
		}
		return loader;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		IOUtils.close(manager);
		options.clear();
		paths.clear();
	}
}

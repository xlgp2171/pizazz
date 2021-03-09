package org.pizazz2.tool;

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

import org.pizazz2.ICloseable;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.common.CollectionUtils;
import org.pizazz2.common.IOUtils;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.PathUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 类动态编译组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PCompiler implements ICloseable {
    private final JavaCompiler compiler;
    /**
     * 诊断信息
     */
    private final DiagnosticCollector<JavaFileObject> collector;
    private final StandardJavaFileManager manager;
    private final Set<String> options = new LinkedHashSet<>();
    private final List<String> paths = new LinkedList<>();
    private final PClassLoader loader;

    public PCompiler(PClassLoader loader) throws ValidateException {
        ValidateUtils.notNull("PCompiler", loader);
        compiler = ToolProvider.getSystemJavaCompiler();
        collector = new DiagnosticCollector<>();
        manager = compiler.getStandardFileManager(collector, null, null);
        this.loader = loader;
    }

    public PCompiler setOptions(String... options) {
        // 增加参数可以跟编译依赖javac -extdirs lib;lib_ext -Xlint:unchecked
        if (!ArrayUtils.isEmpty(options)) {
            this.options.addAll(Arrays.asList(options));
        }
        return this;
    }

    public PCompiler addResource(Path dir, Path name) throws ValidateException, UtilityException {
        ValidateUtils.notNull("addResource", dir, name);
        Path source = dir.resolve(name);

        if (Files.exists(source)) {
            PathUtils.copy(source, loader.getDirectory().resolve(name));
        }
        return this;
    }

    public PCompiler addFile(Path path) throws ValidateException, ToolException {
        ValidateUtils.notNull("addFile", path);

        if (PathUtils.isRegularFile(path) && path.toString().endsWith(JavaFileObject.Kind.SOURCE.extension)) {
            paths.add(path.toString());
        } else {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path);
            throw new ToolException(BasicCodeEnum.MSG_0005, msg);
        }
        return this;
    }

    public PClassLoader compile() throws ToolException {
        options.addAll(Arrays.asList("-d", loader.getDirectory().toString(), "-Xlint:unchecked"));
        Iterable<? extends JavaFileObject> files = manager.getJavaFileObjectsFromStrings(paths);
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, collector, options, null, files);
        Boolean result = task.call();

        if (result == null || !result) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.COMPILE", CollectionUtils.toString(options),
					CollectionUtils.toString(paths), CollectionUtils.toString(collector.getDiagnostics()));
            throw new ToolException(BasicCodeEnum.MSG_0022, msg);
        }
        return loader;
    }

    @Override
    public void destroy(Duration timeout) {
        IOUtils.close(manager);
        options.clear();
        paths.clear();
    }
}

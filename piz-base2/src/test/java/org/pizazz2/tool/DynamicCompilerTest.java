package org.pizazz2.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.tools.JavaFileObject;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.PizContext;
import org.pizazz2.common.*;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;

/**
 * DynamicCompiler测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class DynamicCompilerTest {

	private JavaClass getJavaClass(String resource) throws IOException, UtilityException {
		String code;
		// 加载文件
		try (InputStream _in = IOUtils.getResourceAsStream(resource)) {
			code = IOUtils.readInputStream(_in);
		}
		// 类路径
		String classpath = ClassUtils.getPackageName(code) + "." + ClassUtils.getClassName(code);
		// 生成JAVA文件
		Path tempDir = PathUtils.createTempDirectory(PizContext.NAMING_SHORT + "_CLASS_");
		Path path = PathUtils.createDirectories(tempDir.resolve(classpath.replace(".", "/") +
				JavaFileObject.Kind.SOURCE.extension));

		try (InputStream in = IOUtils.getResourceAsStream(resource)) {
			PathUtils.copyToPath(in, path);
		}
		return new JavaClass(path, classpath);
	}

	@Test
	public void tesCompiler() throws IOException, UtilityException, ToolException {
		String target = "中文";
		String resource = "DynamicObject.tmp";
		JavaClass tmp = getJavaClass(resource);
		PizClassLoader loader = new PizClassLoader(resource, this.getClass().getClassLoader());
		loader.extractJar(PizContext.LOCAL_PATH.resolve("target/piz-base2-2.0.0.jar"));
		// 编译
		try (DynamicCompiler compiler = new DynamicCompiler(loader)) {
			loader = compiler
					.setOptions("-cp", PizContext.LOCAL_PATH.resolve("target/piz-base2-2.0.0.jar").toString())
					.addFile(tmp.path).compile();
		}
		//　反射
		Class<?> clazz = ClassUtils.loadClass(tmp.classpath, loader, true);
		Object instance = ReflectUtils.invokeConstructor(clazz, new Class<?>[] { Object.class },
				new Object[] { target }, false);
		String result = ReflectUtils.invokeMethod(instance, "toString", null, null,
				String.class, false);
		PathUtils.delete(tmp.path, false);
		Assert.assertEquals(result, target);
	}

	private static class JavaClass {
		public Path path;
		public String classpath;

		public JavaClass(Path path, String classpath) {
			this.path = path;
			this.classpath = classpath;
		}
	}
}

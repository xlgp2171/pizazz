package org.pizazz2.tool;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.JavaFileObject;

import org.pizazz2.PizContext;
import org.pizazz2.IObject;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.common.IOUtils;
import org.pizazz2.context.ClassContext;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.PathUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 类加载组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PizClassLoader extends URLClassLoader {
	/**
	 * 未关闭的JAR文件
	 */
	protected final WeakHashMap<Closeable, Void> closeables;
	private final String id;
	private Path dir;
	/**
	 * 文件夹一旦使用则不允许修改
	 */
	private boolean isDirUpdate = true;
	private final Object lock = new Object();

	public PizClassLoader(String id) {
		this(id, null);
	}

	public PizClassLoader(String id, ClassLoader parent) {
		super(new URL[0], parent);
		this.id = id;
		closeables = new WeakHashMap<>();
		ClassContext.getInstance().register(this);
	}

	public String getId() {
		return id;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException e) {
			return toClass(name);
		}
	}

	private Class<?> toClass(String name) throws ClassNotFoundException {
		String path = name.replace('.', '/').concat(JavaFileObject.Kind.CLASS.extension);
		Path classFile = getDirectory().resolve(path);

		if (!Files.isReadable(classFile) || !Files.isRegularFile(classFile)) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", classFile.toAbsolutePath());
			throw new ClassNotFoundException(msg);
		}
		byte[] data;
		try {
			data = PathUtils.toByteArray(classFile);
		} catch (BaseException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		Class<?> tmp = defineClass(name, data, 0, data.length);

		if (tmp == null) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.PATH.REGULAR",
					classFile.toAbsolutePath());
			throw new ClassNotFoundException(msg);
		}
		return tmp;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream in = super.getResourceAsStream(name);

		if (in == null) {
			try {
				in = IOUtils.getInputStream(getDirectory().resolve(name));
			} catch (ValidateException | UtilityException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		return in;
	}

	public synchronized PizClassLoader extractJar(Path path) throws ValidateException, UtilityException, ToolException {
		ValidateUtils.notNull("extractJar", path);

		try (JarFile item = new JarFile(path.toFile())) {
			Enumeration<JarEntry> entries = item.entries();

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				// 兼容能获取文件夹的情况
				if (entry.isDirectory()) {
					PathUtils.createDirectories(getDirectory().resolve(name));
				} else {
					Path tmp = Paths.get(name);

					if (tmp.getParent() != null) {
						Path dir = getDirectory().resolve(tmp.getParent());

						if (!Files.exists(dir)) {
							PathUtils.createDirectories(dir);
						}
					}
					try (InputStream in = item.getInputStream(entry)) {
						PathUtils.copyToPath(in, getDirectory().resolve(name));
					}
				}
			}
			isDirUpdate = false;
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path.toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, msg, e);
		}
		URL url;
		try {
			url = getDirectory().toUri().toURL();
		} catch (MalformedURLException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR",
					getDirectory().toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, msg, e);
		}
		if (!ArrayUtils.contains(super.getURLs(), url)) {
			super.addURL(url);
		}
		return this;
	}

	/**
	 * 链接JAR文件<br>
	 * Paths.get("C:/lib/tools.jar");
	 * 
	 * @param path jar文件路径
	 * @throws ToolException 重复加载异常或链接加载异常
	 * @throws ValidateException 参数验证异常
	 */
	public synchronized PizClassLoader linkJar(Path path) throws ValidateException, ToolException {
		ValidateUtils.notNull("linkJar", path);

		if (!Files.isReadable(path) || !Files.isRegularFile(path)) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path.toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, msg);
		}
		String name = "jar:" + path.toUri() + "!/";
		URL url;
		try {
			url = new URL(name);
		} catch (MalformedURLException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.NAME", name, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0005, msg, e);
		}
		return linkJar(url);
	}

	/**
	 * 链接JAR文件<br>
	 * new URL("jar:file:/C:/lib/tools.jar!/");
	 * 
	 * @param url URL
	 * @throws ToolException 重复加载异常或链接加载异常
	 * @throws ValidateException 参数验证异常
	 */
	public synchronized PizClassLoader linkJar(URL url) throws ValidateException, ToolException {
		ValidateUtils.notNull("linkJar", url);

		if (ArrayUtils.contains(super.getURLs(), url)) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.EXIST", url);
			throw new ToolException(BasicCodeEnum.MSG_0005, msg);
		}
		JarFile file;
		// xlgp2171:是否会造成打开文件过多?
		try {
			JarURLConnection connection = ClassUtils.cast(url.openConnection(), JarURLConnection.class);
			connection.setUseCaches(true);
			/* _connection.connect(); */
			file = connection.getJarFile();
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.IN", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
		}
		synchronized (closeables) {
			super.addURL(url);
			closeables.put(file, null);
		}
		return this;
	}

	public void setDirectory(Path dir) {
		if (isDirUpdate && dir != null && Files.isDirectory(dir)) {
			this.dir = dir;
		}
	}

	protected Path getDirectory() {
		if (dir == null) {
			synchronized (lock) {
				if (dir == null) {
					try {
						dir = PathUtils.createTempDirectory(PizContext.NAMING_SHORT + "_CLASS_");
					} catch (BaseException e) {
						dir = PizContext.LOCAL_PATH;
					}
				}
			}
		}
		return dir;
	}

	@Override
	public void close() {
		isDirUpdate = false;
		try {
			super.close();
		} catch (IOException e) {
			// do nothing
		}
		closeables.keySet().forEach(SystemUtils::close);
		closeables.clear();
		ClassContext.getInstance().unregister(getId());
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}

package org.pizazz.tool;

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

import org.pizazz.Constant;
import org.pizazz.IObject;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.PathUtils;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ErrorCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 类加载组件
 * 
 * @author xlgp2171
 * @version 1.2.190219
 */
public class PClassLoader extends URLClassLoader implements IObject {
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

	public PClassLoader(String id, ClassLoader parent) {
		super(new URL[0], parent);
		this.id = id;
		closeables = new WeakHashMap<Closeable, Void>();
	}

	@Override
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
		String _path = name.replace('.', '/').concat(JavaFileObject.Kind.CLASS.extension);
		Path _classFile = getDirectory().resolve(_path);

		if (!Files.isReadable(_classFile) || !Files.isRegularFile(_classFile)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", _classFile.toAbsolutePath());
			throw new ClassNotFoundException(_msg);
		}
		byte[] _data;
		try {
			_data = PathUtils.toByteArray(_classFile);
		} catch (BaseException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		Class<?> _tmp = defineClass(name, _data, 0, _data.length);

		if (_tmp == null) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.CLASS.PATH.REGULAR",
					_classFile.toAbsolutePath());
			throw new ClassNotFoundException(_msg);
		}
		return _tmp;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream _in = super.getResourceAsStream(name);

		if (_in == null) {
			try {
				_in = PathUtils.getInputStream(getDirectory().resolve(name));
			} catch (BaseException e) {
				throw new BaseError(ErrorCodeEnum.ERR_0002, e);
			}
		}
		return _in;
	}

	public synchronized PClassLoader extractJAR(Path path) throws AssertException, UtilityException, ToolException {
		AssertUtils.assertNotNull("extractJAR", path);

		try (JarFile _item = new JarFile(path.toFile())) {
			Enumeration<JarEntry> _entries = _item.entries();

			while (_entries.hasMoreElements()) {
				JarEntry _entry = _entries.nextElement();
				String _name = _entry.getName();
				// 兼容能获取文件夹的情况
				if (_entry.isDirectory()) {
					PathUtils.createDirectories(getDirectory().resolve(_name));
				} else {
					Path _tmp = Paths.get(_name);

					if (_tmp.getParent() != null) {
						Path _dir = getDirectory().resolve(_tmp.getParent());

						if (!Files.exists(_dir)) {
							PathUtils.createDirectories(_dir);
						}
					}
					try (InputStream _in = _item.getInputStream(_entry)) {
						PathUtils.copyToPath(getDirectory().resolve(_name), _in);
					}
				}
			}
			isDirUpdate = false;
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path.toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, _msg, e);
		}
		URL _url;
		try {
			_url = getDirectory().toUri().toURL();
		} catch (MalformedURLException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR",
					getDirectory().toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, _msg, e);
		}
		if (!ArrayUtils.contains(super.getURLs(), _url)) {
			super.addURL(_url);
		}
		return this;
	}

	/**
	 * 链接JAR文件<br>
	 * Paths.get("C:/lib/tools.jar");
	 * 
	 * @param path
	 * @throws ToolException
	 * @throws UtilityException
	 * @throws AssertException
	 */
	public synchronized PClassLoader linkJAR(Path path) throws AssertException, ToolException, UtilityException {
		AssertUtils.assertNotNull("linkJAR", path);

		if (!Files.isReadable(path) || !Files.isRegularFile(path)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path.toAbsolutePath());
			throw new ToolException(BasicCodeEnum.MSG_0005, _msg);
		}
		String _name = "jar:" + path.toUri() + "!/";
		URL _url = null;
		try {
			_url = new URL(_name);
		} catch (MalformedURLException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.NAME", _name, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0005, _msg, e);
		}
		return linkJAR(_url);
	}

	/**
	 * 链接JAR文件<br>
	 * new URL("jar:file:/C:/lib/tools.jar!/");
	 * 
	 * @param url
	 * @throws ToolException
	 * @throws AssertException
	 * @throws UtilityException
	 */
	public synchronized PClassLoader linkJAR(URL url) throws AssertException, ToolException, UtilityException {
		AssertUtils.assertNotNull("linkJAR", url);

		if (ArrayUtils.contains(super.getURLs(), url)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.EXIST", url);
			throw new ToolException(BasicCodeEnum.MSG_0005, _msg);
		}
		JarFile _file;
		// FIXME xlgp2171:是否会造成打开文件过多?
		try {
			JarURLConnection _connection = ClassUtils.cast(url.openConnection(), JarURLConnection.class);
			_connection.setUseCaches(true);
			// _connection.connect();
			_file = _connection.getJarFile();
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.IN", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		synchronized (closeables) {
			super.addURL(url);
			closeables.put(_file, null);
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
						dir = PathUtils.createTempDirectory(Constant.NAMING_SHORT + "_CLASS_");
					} catch (BaseException e) {
						dir = SystemUtils.LOCAL_DIR;
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
		}
		closeables.keySet().forEach(_item -> IOUtils.close(_item));
		closeables.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}

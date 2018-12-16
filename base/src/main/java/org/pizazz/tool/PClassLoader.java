package org.pizazz.tool;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarFile;

import org.pizazz.IObject;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.SystemUtils;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ref.TypeEnum;

/**
 * 类加载组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class PClassLoader extends URLClassLoader implements IObject {
	private final Map<URL, JarFile> paths;
	private final String id;
	private Path dir = SystemUtils.LOCAL_DIR;

	public PClassLoader(String id, ClassLoader parent) {
		super(new URL[0], parent);
		this.id = id;
		paths = new Hashtable<URL, JarFile>();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> _tmp = super.findClass(name);

		if (_tmp == null) {
			_tmp = toClass(name);
		}
		return _tmp;
	}

	private Class<?> toClass(String name) throws ClassNotFoundException {
		String _path = name.replace('.', '/').concat(".class");
		Path _classFile = dir.resolve(_path);

		if (!Files.isReadable(_classFile) || !Files.isRegularFile(_classFile)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", _classFile.toAbsolutePath());
			throw new ClassNotFoundException(_msg);
		}
		byte[] _data;
		try {
			_data = Files.readAllBytes(_classFile);
		} catch (IOException e) {
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

	/**
	 * new File("C:/lib/tools.jar");
	 * 
	 * @param path
	 * @throws BaseException
	 */
	public PClassLoader addJar(Path path) throws BaseException {
		AssertUtils.assertNotNull("addJar", path);

		if (!Files.isReadable(path) || !Files.isRegularFile(path)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.REGULAR", path.toAbsolutePath());
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
		}
		String _name = "jar:" + path.toUri() + "!/";
		URL _url = null;
		try {
			_url = new URL(_name);
		} catch (MalformedURLException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.NAME", _name, e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg, e);
		}
		return addJar(_url);
	}

	/**
	 * new URL("jar:file:/C:/lib/tools.jar!/");
	 * 
	 * @param url
	 * @throws BaseException
	 */
	public PClassLoader addJar(URL url) throws BaseException {
		AssertUtils.assertNotNull("addJar", url);

		synchronized (paths) {
			if (paths.containsKey(url)) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.JAR.EXIST", url);
				throw new BaseException(_msg);
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
				throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
			}
			super.addURL(url);
			paths.put(url, _file);
		}
		return this;
	}

	public void setClassDirectory(Path dir) {
		if (dir != null && Files.isDirectory(dir)) {
			this.dir = dir;
		}
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (IOException e) {
		}
		paths.values().forEach(_item -> IOUtils.close(_item));
		paths.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}

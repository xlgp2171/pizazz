package org.pizazz.common;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 文件工具
 * 
 * @author xlgp2171
 * @version 1.2.191013
 */
public class PathUtils {

	public static URI toURI(String uri) throws BaseException {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "BASIC.ERR.PATH.FORMAT", "URI", uri,
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0005, _msg, e);
		}
	}

	public static byte[] toByteArray(Path path) throws BaseException {
		AssertUtils.assertNotNull("toByteArray", path);
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	public static InputStream getInputStream(Path path) throws BaseException {
		AssertUtils.assertNotNull("getInputStream", path);
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	public static void delete(Path path, boolean deep) throws BaseException {
		AssertUtils.assertNotNull("delete", path);

		if (Files.isDirectory(path)) {
			if (deep) {
				deleteDirectory(path);
			}
		} else {
			try {
				Files.delete(path);
			} catch (IOException e) {
			}
		}
	}

	public static Path[] listPaths(Path dir, Predicate<Path> filter, boolean includeDir) throws BaseException {
		try (Stream<Path> _stream = Files.walk(dir)) {
			Stream<Path> _tmp = _stream;

			if (includeDir) {
				if (filter != null) {
					_tmp = _tmp.filter(_item -> filter.test(_item));
				}
			} else {
				if (filter != null) {
					_tmp = _tmp.filter(
							_item -> Files.isRegularFile(_item) && Files.isReadable(_item) && filter.test(_item));
				} else {
					_tmp = _tmp.filter(_item -> Files.isRegularFile(_item) && Files.isReadable(_item));
				}
			}
			return _tmp.toArray(Path[]::new);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", dir.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	static void deleteDirectory(Path dir) throws BaseException {
		DirectoryStream<Path> _tmp;
		try {
			_tmp = Files.newDirectoryStream(dir);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", dir.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		for (Path _item : _tmp) {
			delete(_item, true);
		}
	}

	public static long copy(FileInputStream in, FileOutputStream out) throws BaseException {
		AssertUtils.assertNotNull("copy", in, out);

		try (FileChannel _in = in.getChannel(); FileChannel _out = out.getChannel()) {
			// 将fileChannelInput通道的数据，写入到fileChannelOutput通道
			return _in.transferTo(0, _in.size(), _out);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		} finally {
			IOUtils.close(in);
			IOUtils.close(out);
		}
	}

	public static long copyToPath(Path path, InputStream in) throws BaseException {
		AssertUtils.assertNotNull("copyToPath", path, in);
		try {
			return Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH", path.toAbsolutePath(), e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	public static Path createDirectories(Path dir) throws BaseException {
		AssertUtils.assertNotNull("createDirectories", dir);
		try {
			return Files.createDirectories(dir);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR", dir.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	public static Path createTempDirectory(String prefix) throws BaseException {
		try {
			return Files.createTempDirectory(prefix);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR.TEMP", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	public static Path copyToTemp(Path path, String prefix) throws BaseException {
		AssertUtils.assertNotNull("copyToTemp", path);
		return copyToTemp(toByteArray(path), prefix);
	}

	public static Path copyToTemp(byte[] data, String prefix) throws BaseException {
		AssertUtils.assertNotNull("copyToTemp", data);
		Path _tmp;
		try {
			_tmp = Files.createTempFile(prefix, ".tmp");
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.TEMP", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		try (ByteArrayInputStream _in = new ByteArrayInputStream(data)) {
			Files.copy(_in, _tmp, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.WRITE", _tmp.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		return _tmp;
	}
}

package org.pizazz.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.LocaleHelper;
import org.pizazz.message.ref.TypeEnum;

/**
 * 文件工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class PathUtils {

	public static byte[] toByteArray(Path path) throws BaseException {
		try {
			return Files.readAllBytes(path);
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
}

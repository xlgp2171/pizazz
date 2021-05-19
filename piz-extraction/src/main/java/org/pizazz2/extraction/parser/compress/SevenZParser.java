package org.pizazz2.extraction.parser.compress;

import org.apache.commons.compress.PasswordRequiredException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 7Z(application/x-7z-compressed)解析<br>
 * 无解析属性Metadata<br>
 * 可设置外部密码
 *
 * @author xlgp2
 * @version 2.0.210501
 */
public class SevenZParser extends AbstractCompressParser {

	@Override
	protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
			ValidateException, DetectionException {
		AbstractCompressParser.Config tmp = config.getTarget(AbstractCompressParser.Config.class);
		try {
			doUncompress(object, tmp.password(), tmp.includeDirectory());
		} catch (PasswordRequiredException e) {
			object.setStatus(ExtractObject.StatusEnum.ENCRYPTION);
		} catch (IOException e) {
			super.throwException(object, tmp, e);
		}
	}

	private void doUncompress(ExtractObject object, String password, boolean includeDirectory)
			throws IOException, PasswordRequiredException {
		char[] tmp = StringUtils.isTrimEmpty(password) ? null : password.toCharArray();

		try (SeekableInMemoryByteChannel memory = new SeekableInMemoryByteChannel(object.getData());
				SevenZFile file = new SevenZFile(memory, tmp)) {
			SevenZArchiveEntry entry = file.getNextEntry();
			Path parent = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());

			while (entry != null) {
				Path path = Paths.get(entry.getName());

				if (entry.isDirectory()) {
					// 存储基础文件夹
					if (includeDirectory) {
						// 存储基础文件夹
						super.addAttachment(object, StringUtils.EMPTY, (parent == null ? path : parent.resolve(path))
								.toString()).setStatus(ExtractObject.StatusEnum.EMPTY);
					}
				} else {
					int length = new Long(entry.getSize()).intValue();
					byte[] data = new byte[length];
					file.read(data, 0, length);
					super.addAttachment(object, path.getFileName().toString(), ExtractHelper.pathResolve(parent, path))
							.setData(data);
				}
				entry = file.getNextEntry();
			}
		}
	}

	@Override
	public String[] getType() {
		return new String[] { "application/x-7z-compressed" };
	}
}

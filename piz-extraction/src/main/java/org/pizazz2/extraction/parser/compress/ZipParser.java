package org.pizazz2.extraction.parser.compress;

import org.apache.commons.compress.archivers.zip.GeneralPurposeBit;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.pizazz2.PizContext;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 * ZIP(application/zip)解析<br>
 * 无解析属性Metadata<br>
 * 若要采用zip加密，推荐使用zip4j，但zip4j无法通过内存加载
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public class ZipParser extends AbstractCompressParser {
    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener)
			throws ParseException, ValidateException, IllegalException, DetectionException {
		AbstractCompressParser.Config tmp = config.getTarget(AbstractCompressParser.Config.class);
		try {
			if (encrypted(object.getData())) {
				object.setStatus(ExtractObject.StatusEnum.ENCRYPTION);
			} else {
				try {
					doUncompress(object, tmp.charset(), tmp.includeDirectory());
				} catch (MalformedInputException e1) {
					Charset charset = super.detect(object.getData(), config.detectLimit(), PizContext.LOCAL_ENCODING);
					// 解压失败则用标准编码再解压
					doUncompress(object, charset, tmp.includeDirectory());
				}
			}
		} catch (Exception e2) {
			super.throwException(object, tmp, e2);
		}
    }

    private boolean encrypted(byte[] data) throws IOException {
        try (SeekableInMemoryByteChannel memory = new SeekableInMemoryByteChannel(data);
			 ZipFile file = new ZipFile(memory)) {
            Enumeration<ZipArchiveEntry> entries = file.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                GeneralPurposeBit bit = entry.getGeneralPurposeBit();
                // 被认为任何一个文件加密则整个文件加密
                if (!entry.isDirectory()) {
                    return bit.usesEncryption() || bit.usesStrongEncryption();
                }
            }
        }
        return false;
    }

    protected void doUncompress(ExtractObject object, Charset charset, boolean includeDirectory)
			throws IOException, UtilityException {
        try (SeekableInMemoryByteChannel memory = new SeekableInMemoryByteChannel(object.getData());
			 ZipFile file = new ZipFile(memory, charset.name())) {
            Enumeration<ZipArchiveEntry> entries = file.getEntries();
            Path parent = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path path = Paths.get(entry.getName());

                if (entry.isDirectory()) {
                    // 存储基础文件夹
                    if (includeDirectory) {
                        // 存储基础文件夹
                        super.addAttachment(object, StringUtils.EMPTY, (parent == null ? path : parent.resolve(path))
                                .toString()).setStatus(ExtractObject.StatusEnum.EMPTY);
                    }
                } else {
                    super.addAttachment(object, path.getFileName().toString(), ExtractHelper.pathResolve(parent, path))
                            .setData(IOUtils.toByteArray(file.getInputStream(entry)));
                }
            }
        }
    }

    @Override
    public String[] getType() {
        return new String[] { "application/zip" };
    }
}

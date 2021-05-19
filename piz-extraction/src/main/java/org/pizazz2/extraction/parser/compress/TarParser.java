package org.pizazz2.extraction.parser.compress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tika.mime.MediaType;
import org.pizazz2.PizContext;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;

/**
 * TAR(application/x-tar)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public class TarParser extends AbstractCompressParser {

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, DetectionException {
        AbstractCompressParser.Config tmp = config.getTarget(AbstractCompressParser.Config.class);
        try {
            try {
                doUncompress(object, tmp.charset(), tmp.includeDirectory());
            } catch (MalformedInputException e1) {
                Charset charset = super.detect(object.getData(), config.detectLimit(), PizContext.LOCAL_ENCODING);
                // 解压失败则用标准编码再解压
                doUncompress(object, charset, tmp.includeDirectory());
            }
        } catch (Exception e2) {
            super.throwException(object, config, e2);
        }
    }

    protected void doUncompress(ExtractObject object, Charset charset, boolean includeDirectory) throws IOException,
            DetectionException, ParseException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(object.getData());
             TarArchiveInputStream stream = new TarArchiveInputStream(in, charset.name())) {
            TarArchiveEntry entry = stream.getNextTarEntry();
            Path parent = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());

            while (entry != null) {
                Path path = Paths.get(entry.getName());

                if (entry.isDirectory()) {
                    if (includeDirectory) {
                        // 存储基础文件夹
                        super.addAttachment(object, StringUtils.EMPTY, (parent == null ? path : parent.resolve(path))
                                .toString()).setStatus(ExtractObject.StatusEnum.EMPTY);
                    }
                } else {
                    int length = new Long(entry.getSize()).intValue();
                    byte[] data = new byte[length];

                    if (stream.read(data) > 0) {
                        super.addAttachment(object, path.getFileName().toString(), ExtractHelper.pathResolve(
                                parent, path)).setData(data);
                    }
                }
                entry = stream.getNextTarEntry();
            }
        }
    }

    protected void doUncompressSub(ExtractObject object, InputStream in, Charset charset, boolean includeDirectory)
            throws ParseException, DetectionException, IOException {
        byte[] data;
        try {
            data = IOUtils.toByteArray(in);
        } catch (UtilityException e) {
            throw new ParseException(e.getMessage(), e );
        }
        MediaType type = super.detect(ExtractHelper.newTempObject().setData(data));
        // 若数据识别为"application/x-tar"
        if (getType()[0].equals(StringUtils.of(type))) {
            doUncompress(object.setData(data), charset,includeDirectory);
        } else {
            // 当压缩文件为单独时，name使用文件名称代替
            super.addAttachment(object, object.getName(), object.getSource()).setType(type).setData(data);
        }
    }

    @Override
    public String[] getType() {
        return new String[] { "application/x-tar" };
    }
}

package org.pizazz2.extraction.parser.compress;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.pizazz2.common.StringUtils;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * GZIP(application/gzip)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2171
 * @version 2.1.220714
 */
public class GzipParser extends TarParser {

	@Override
	public String[] getType() {
		return new String[] { "application/gzip" };
	}

	@Override
	protected void doUncompress(ExtractObject object, Charset charset, boolean includeDirectory,
								boolean idNamedDirectory) throws IOException,
			DetectionException, ParseException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(object.getData());
				GzipCompressorInputStream gin = new GzipCompressorInputStream(in)) {
			super.doUncompressSub(object, gin, charset, includeDirectory, idNamedDirectory);
		}
	}

	@Override
	protected String getSubName(ExtractObject object, InputStream in) {
		String name = null;

		if (in instanceof GzipCompressorInputStream) {
			GzipCompressorInputStream gin = (GzipCompressorInputStream) in;
			name = gin.getMetaData().getFilename();
		}
		if (StringUtils.isEmpty(name)) {
			name = super.getSubName(object, in);
		}
		return name;
	}
}

package org.pizazz2.extraction.parser.compress;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * GZIP(application/gzip)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public class GzipParser extends TarParser {

	@Override
	public String[] getType() {
		return new String[] { "application/gzip" };
	}

	@Override
	protected void doUncompress(ExtractObject object, Charset charset, boolean includeDirectory) throws IOException,
			DetectionException, ParseException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(object.getData());
				GzipCompressorInputStream gin = new GzipCompressorInputStream(in)) {
			super.doUncompressSub(object, gin, charset, includeDirectory);
		}
	}
}

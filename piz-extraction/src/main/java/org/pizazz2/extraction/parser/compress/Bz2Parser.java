package org.pizazz2.extraction.parser.compress;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Bz2(application/x-bzip2)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2
 * @version 2.0.210501
 */
public class Bz2Parser extends TarParser {

	@Override
	public String[] getType() {
		return new String[]{ "application/x-bzip2" };
	}

	@Override
	protected void doUncompress(ExtractObject object, Charset charset, boolean includeDirectory) throws IOException,
			DetectionException, ParseException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(object.getData());
			 BZip2CompressorInputStream bin = new BZip2CompressorInputStream(in)) {
			super.doUncompressSub(object, bin, charset, includeDirectory);
		}
	}
}

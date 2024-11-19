package org.pizazz2.extraction.parser.audio;

import com.argo.hwp.v3.HwpTextExtractorV3;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.mime.MediaType;
import org.gagravarr.flac.FlacFile;
import org.gagravarr.flac.FlacInfo;
import org.gagravarr.flac.FlacOggFile;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.support.ExtractHelper;

import java.io.ByteArrayInputStream;

/**
 * Flac解析<br>
 * 由于org.gagravarr.tika。FlacParser输出属性不足
 *
 * @author xlgp2171
 * @version 2.3.241029
 */
public class FlacParser extends AbstractParser {

	protected static final MediaType NATIVE_FLAC =
			MediaType.parse(OggStreamIdentifier.NATIVE_FLAC.mimetype);
	protected static final MediaType OGG_FLAC =
			MediaType.parse(OggStreamIdentifier.OGG_FLAC.mimetype);

	@Override
	protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
			ValidateException, DetectionException {
		Metadata metadata = object.getMetadata();
		metadata.set(XMPDM.AUDIO_COMPRESSOR, "FLAC");
		// Open the FLAC file
		try (FlacFile flac = FlacFile.open(new ByteArrayInputStream(object.getData()))) {
			FlacInfo info = flac.getInfo();
			metadata.set("channel", "2");
			metadata.set(XMPDM.AUDIO_SAMPLE_RATE, info.getSampleRate());
			metadata.set(XMPDM.DURATION, info.getNumberOfSamples() * 1.0 / info.getSampleRate());

			if(flac instanceof FlacOggFile) {
				FlacOggFile ogg = (FlacOggFile)flac;
				metadata.add("version", "Flac " + ogg.getFirstPacket().getMajorVersion() +
						"." + ogg.getFirstPacket().getMinorVersion());
				metadata.set(Metadata.CONTENT_TYPE, OGG_FLAC.toString());
			} else {
				metadata.set(Metadata.CONTENT_TYPE, NATIVE_FLAC.toString());
			}
		} catch (Exception e) {
			super.throwException(object, config, e);
		}
	}

	@Override
	public String[] getType() {
		return new String[] { NATIVE_FLAC.toString(), OGG_FLAC.toString() };
	}
}

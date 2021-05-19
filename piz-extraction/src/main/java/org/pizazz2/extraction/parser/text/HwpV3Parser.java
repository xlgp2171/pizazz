package org.pizazz2.extraction.parser.text;

import com.argo.hwp.v3.HwpTextExtractorV3;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.support.ExtractHelper;

/**
 * HWP(application/x-hwp)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public class HwpV3Parser extends AbstractParser {

	@Override
	protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
			ValidateException, DetectionException {
		try {
			String content = HwpTextExtractorV3.extractText(object.getData());
			object.setContent(ExtractHelper.tryCleanBlankLine(content, config.cleanLine()));
		} catch (Exception e) {
			super.throwException(object, config, e);
		}
	}

	@Override
	public String[] getType() {
		return new String[] { "application/x-hwp" };
	}
}

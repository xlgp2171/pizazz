package org.pizazz2.extraction.parser.compress;

import org.pizazz2.PizContext;
import org.pizazz2.common.StringUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.config.ParseConfig;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.helper.TupleObjectHelper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 解压缩基类
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public abstract class AbstractCompressParser extends AbstractParser {

	@Override
	public IConfig toConfig(TupleObject config) {
		return new Config(config);
	}

	public static class Config extends ParseConfig {
		private final boolean includeDirectory;
		private final String password;

		public Config(TupleObject config) throws ValidateException {
			super(config);
			// 附件是否包括文件夹
			this.includeDirectory = TupleObjectHelper.getBoolean(config, "includeDirectory", false);
			// 解包密码
			this.password = TupleObjectHelper.getString(config, "password", null);
		}

		public boolean includeDirectory() {
			return includeDirectory;
		}

		public String password() {
			return password;
		}
	}
}

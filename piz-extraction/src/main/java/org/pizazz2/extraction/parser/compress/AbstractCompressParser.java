package org.pizazz2.extraction.parser.compress;

import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.config.ParseConfig;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.helper.TupleObjectHelper;


/**
 * 解压缩基类
 *
 * @author xlgp2171
 * @version 2.1.220714
 */
public abstract class AbstractCompressParser extends AbstractParser {

	@Override
	public IConfig toConfig(TupleObject config) {
		return new Config(config);
	}

	public static class Config extends ParseConfig {
		private final boolean includeDirectory;
		private final String password;
		private final boolean idNamedDirectory;

		public Config(TupleObject config) throws IllegalException {
			super(config);
			// 附件是否包括文件夹
			this.includeDirectory = TupleObjectHelper.getBoolean(config, "includeDirectory", false);
			// 解包密码
			this.password = TupleObjectHelper.getString(config, "password", null);
			// 使用ID命名目录
			this.idNamedDirectory = TupleObjectHelper.getBoolean(config, "idNamedDirectory", Boolean.TRUE);
		}

		public boolean includeDirectory() {
			return includeDirectory;
		}

		public String password() {
			return password;
		}

		public boolean idNamedDirectory() {
			return idNamedDirectory;
		}
	}
}

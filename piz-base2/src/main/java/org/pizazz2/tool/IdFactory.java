package org.pizazz2.tool;

import java.util.concurrent.atomic.AtomicReference;

import org.pizazz2.PizContext;
import org.pizazz2.common.NumberUtils;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.tool.ref.IIdFactory;
import org.pizazz2.tool.ref.IdObject;

/**
 * ID生成工厂<br>
 * 参考vesta-id实现 60位[1(占位符)][3(识别码)][40(时间戳)][10(顺序号)][6(自定义)]<br>
 * 
 * @author xlgp2171
 * @version 2.1.211028
 */
public class IdFactory implements IIdFactory {
	public static final byte BIT_TOP = 1;
	public static final byte BIT_VERSION = 3;
	public static final byte BIT_CUSTOM = 6;
	public static final byte BIT_SEQUENCE = 10;
	public static final byte BIT_TIMESTAMP = 40;

	public static IdBuilder DEFAULT = IdFactory.newInstance();

	private final AtomicReference<Sequence> cache = new AtomicReference<>(new Sequence());

	public static IdBuilder newInstance() {
		return new IdBuilder(Singleton.INSTANCE.get(), NumberUtils.ZERO.shortValue());
	}

	public static IdBuilder newInstance(short custom) {
		return new IdBuilder(Singleton.INSTANCE.get(), custom);
	}

	public static IdObject parseObject(long id) throws ValidateException {
		return Singleton.INSTANCE.get().parse(id);
	}

	@Override
	public IdObject create(short custom) throws ValidateException {
		custom = validateCustom(custom);
		return new IdObject(PizContext.VERSION, custom);
	}

	@Override
	public long generate(IdObject id) throws ValidateException {
		long tmp = 0L;
		byte offset = 0;
		fill(id);
		tmp |= (long) id.getCustom() << offset;
		tmp |= (long) id.getSequence() << (offset += BIT_CUSTOM);
		tmp |= id.getTimestamp() << (offset += BIT_SEQUENCE);
		tmp |= (long) id.getVersion() << (offset += BIT_TIMESTAMP);
		tmp |= (long) BIT_TOP << offset + BIT_VERSION;
		return tmp;
	}

	@Override
	public IdObject parse(long id) throws ValidateException {
		byte offset = BIT_TOP + BIT_VERSION + BIT_TIMESTAMP + BIT_SEQUENCE + BIT_CUSTOM;
		byte top = parse(id, offset -= BIT_TOP, BIT_TOP).byteValue();
		byte version = parse(id, offset -= BIT_VERSION, BIT_VERSION).byteValue();

		if (top != NumberUtils.ONE.byteValue()) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "top", "BIT(" + BIT_TOP + ")");
			throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
		} else if (version != PizContext.VERSION) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "version",
					"BIT(" + BIT_VERSION + ")");
			throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
		}
		long timestamp = parse(id, offset -= BIT_TIMESTAMP, BIT_TIMESTAMP);
		short sequence = parse(id, offset -= BIT_SEQUENCE, BIT_SEQUENCE).shortValue();
		short custom = parse(id, offset -= BIT_CUSTOM, BIT_CUSTOM).shortValue();
		IdObject tmp = new IdObject(version, custom);
		tmp.setTimestamp(timestamp + IIdFactory.ELAPSE);
		tmp.setSequence(sequence);
		return tmp;
	}

	private Long parse(long id, long offset, long limit) {
		return id >>> offset & (~(-1L << limit));
	}

	public short validateCustom(short custom) throws ValidateException {
		if (custom >> BIT_CUSTOM > 0) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "custom",
					"BIT(" + BIT_CUSTOM + ")");
			throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
		}
		return custom;
	}

	public long validateTimestamp(long timestamp) throws ValidateException {
		long tmp = System.currentTimeMillis() - IIdFactory.ELAPSE;

		if (tmp < timestamp) {
			// 时间以最新时间为主
			tmp = timestamp;
		}
		if (tmp >> BIT_TIMESTAMP > 0) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "timestamp",
					"BIT(" + BIT_TIMESTAMP + ")");
			throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
		}
		return tmp;
	}

	private void fill(IdObject id) throws ValidateException {
		Sequence oldS;
		Sequence newS;
		// 要设置的timestamp
		long timestamp;
		// 要设置的sequence
		short sequence;

		while (true) {
			oldS = cache.get();
			timestamp = validateTimestamp(oldS.timestamp);
			sequence = oldS.sequence;

			if (timestamp == oldS.timestamp) {
				sequence++;
				sequence &= ~(-1L << BIT_SEQUENCE);
				// 若超出预设范围,获取下一个时间
				if (sequence == 0) {
					long tmp = timestamp;
					do {
						timestamp = System.currentTimeMillis() - IIdFactory.ELAPSE;
					} while (tmp >= timestamp);
				}
			} else {
				sequence = 0;
			}
			newS = new Sequence();
			newS.sequence = sequence;
			newS.timestamp = timestamp;

			if (cache.compareAndSet(oldS, newS)) {
				id.setSequence(sequence);
				id.setTimestamp(timestamp);
				break;
			}
		}
	}

	private static class Sequence {
		short sequence = NumberUtils.ZERO.shortValue();
		long timestamp = NumberUtils.NEGATIVE_ONE.longValue();
	}

	public enum Singleton {
		/**
		 * 单例
		 */
		INSTANCE;

		private final IdFactory factory;

		Singleton() {
			factory = new IdFactory();
		}

		public IdFactory get() {
			return factory;
		}
	}
}

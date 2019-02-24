package org.pizazz.tool;

import java.util.concurrent.atomic.AtomicReference;

import org.pizazz.Constant;
import org.pizazz.common.LocaleHelper;
import org.pizazz.exception.AssertException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;
import org.pizazz.tool.ref.IIdFactory;
import org.pizazz.tool.ref.IdObject;

/**
 * ID生成工厂<br>
 * 参考vesta-id实现 60位[1(占位符)][3(识别码)][40(时间戳)][10(顺序号)][6(自定义)]<br>
 * 
 * @author xlgp2171
 * @version 1.0.190224
 *
 */
public class PIdFactory implements IIdFactory {
	public static final byte BIT_TOP = 1;
	public static final byte BIT_VERSION = 3;
	public static final byte BIT_CUSTOM = 6;
	public static final byte BIT_SEQUENCE = 10;
	public static final byte BIT_TIMESTAMP = 40;

	private final AtomicReference<Sequence> cache = new AtomicReference<PIdFactory.Sequence>(new Sequence());

	public static PIdBuilder newInstance() {
		return new PIdBuilder(Singleton.INSTANCE.get());
	}

	public static PIdBuilder newInstance(short custom) {
		return new PIdBuilder(Singleton.INSTANCE.get(), custom);
	}

	public static IdObject parseObject(long id) throws AssertException {
		return Singleton.INSTANCE.get().parse(id);
	}

	@Override
	public IdObject create(short custom) throws AssertException {
		custom = validateCustom(custom);
		return new IdObject(Constant.VERSION, custom);
	}

	@Override
	public long generate(IdObject id) throws AssertException {
		long _tmp = 0L;
		byte _offset = 0;
		fill(id);
		_tmp |= (long) id.getCustom() << _offset;
		_tmp |= (long) id.getSequence() << (_offset += BIT_CUSTOM);
		_tmp |= (long) id.getTimestamp() << (_offset += BIT_SEQUENCE);
		_tmp |= (long) id.getVersion() << (_offset += BIT_TIMESTAMP);
		_tmp |= (long) BIT_TOP << (_offset += BIT_VERSION);
		return _tmp;
	}

	@Override
	public IdObject parse(long id) throws AssertException {
		byte _offset = BIT_TOP + BIT_VERSION + BIT_TIMESTAMP + BIT_SEQUENCE + BIT_CUSTOM;
		byte _top = parse(id, _offset -= BIT_TOP, BIT_TOP).byteValue();
		byte _version = parse(id, _offset -= BIT_VERSION, BIT_VERSION).byteValue();

		if (_top != 1) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "top", "BIT(" + BIT_TOP + ")");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		} else if (_version != Constant.VERSION) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "version",
					"BIT(" + BIT_VERSION + ")");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		long _timestamp = parse(id, _offset -= BIT_TIMESTAMP, BIT_TIMESTAMP);
		short _sequence = parse(id, _offset -= BIT_SEQUENCE, BIT_SEQUENCE).shortValue();
		short _custom = parse(id, _offset -= BIT_CUSTOM, BIT_CUSTOM).shortValue();
		IdObject _tmp = new IdObject(_version, _custom);
		_tmp.setTimestamp(_timestamp + IIdFactory.ELAPSE);
		_tmp.setSequence(_sequence);
		return _tmp;
	}

	private Long parse(long id, long offset, long limit) {
		return new Long(id >>> offset) & (-1L ^ -1L << limit);
	}

	public short validateCustom(short custom) throws AssertException {
		if (custom >> BIT_CUSTOM > 0) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "custom",
					"BIT(" + BIT_CUSTOM + ")");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		return custom;
	}

	public long validateTimestamp(long timestamp) throws AssertException {
		long _tmp = System.currentTimeMillis() - IIdFactory.ELAPSE;

		if (_tmp < timestamp) {
			// 时间以最新时间为主
			_tmp = timestamp;
		}
		if (_tmp >> BIT_TIMESTAMP > 0) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.EQUALS", "timestamp",
					"BIT(" + BIT_TIMESTAMP + ")");
			throw new AssertException(BasicCodeEnum.MSG_0005, _msg);
		}
		return _tmp;
	}

	private void fill(IdObject id) throws AssertException {
		Sequence _old;
		Sequence _new;
		// 要设置的timestamp
		long _timestamp;
		// 要设置的sequence
		short _sequence;

		while (true) {
			_old = cache.get();
			_timestamp = validateTimestamp(_old.timestamp);
			_sequence = _old.sequence;

			if (_timestamp == _old.timestamp) {
				_sequence++;
				_sequence &= -1L ^ -1L << BIT_SEQUENCE;
				// 若超出预设范围,获取下一个时间
				if (_sequence == 0) {
					long _tmp = _timestamp;
					do {
						_timestamp = System.currentTimeMillis() - IIdFactory.ELAPSE;
					} while (_tmp >= _timestamp);
				}
			} else {
				_sequence = 0;
			}
			_new = new Sequence();
			_new.sequence = _sequence;
			_new.timestamp = _timestamp;

			if (cache.compareAndSet(_old, _new)) {
				id.setSequence(_sequence);
				id.setTimestamp(_timestamp);
				break;
			}
		}
	}

	private class Sequence {
		short sequence = 0;
		long timestamp = -1;
	}

	public static enum Singleton {
		INSTANCE;

		private PIdFactory factory;

		private Singleton() {
			factory = new PIdFactory();
		}

		public PIdFactory get() {
			return factory;
		}
	}
}

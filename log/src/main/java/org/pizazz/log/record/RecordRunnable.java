package org.pizazz.log.record;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pizazz.Constant;
import org.pizazz.IPlugin;
import org.pizazz.IRunnable;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.log.ref.TypeEnum;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.tool.AbstractClassPlugin;
import org.pizazz.tool.PThreadFactory;

/**
 * 日志监控线程
 * 
 * @author xlgp2171
 * @version 1.1.190220
 */
public class RecordRunnable extends AbstractClassPlugin implements IPlugin, IRunnable {
	// 日志信息缓存
	private BlockingQueue<RecordEntity> cache;
	// 日志记录循环限制
	private final AtomicBoolean loop = new AtomicBoolean(false);
	// 日志监控接口
	private ILoggerRecord record;
	// 最大缓存数量
	private int maxsize;
	//
	private ExecutorService thread;

	@Override
	public void initialize(TupleObject config) throws BaseException {
		setConfig(config);
		IPlugin _plugin = super.loadPlugin("$CLASS", null, null, true);
		try {
			record = super.cast(_plugin, ILoggerRecord.class);
		} catch (BaseException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.LOG, "RECORD.UNUSE");
			throw new BaseException(BasicCodeEnum.MSG_0019, _msg, e);
		}
		if (loop.compareAndSet(false, record != null)) {
			record.initialize(super.getConfig());
			maxsize = TupleObjectHelper.getInt(config, "$SIZE", 1000);
			cache = new LinkedBlockingQueue<RecordEntity>(new Double(maxsize * 1.2).intValue());
			//
			thread = Executors.newSingleThreadExecutor(new PThreadFactory(Constant.NAMING_SHORT + "-log", true));
			log(LocaleHelper.toLocaleText(TypeEnum.LOG, "RECORD.INIT", getId(), config.toString()), null);
		}
	}

	@Override
	public String getId() {
		return record != null ? record.getClass().getName() : getClass().getName();
	}

	@Override
	public void run() {
		while (loop.get() || !cache.isEmpty()) {
			while (cache.size() > maxsize) {
				// 丢弃
				cache.poll();
			}
			RecordEntity _entity;
			try {
				_entity = cache.take();
			} catch (InterruptedException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.LOG, "ERR.RECORD.TAKE", e.getMessage());
				log(_msg, new BaseException(BasicCodeEnum.MSG_0019, e));
				continue;
			}
			try {
				record.record(_entity);
			} catch (Exception e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.LOG, "ERR.RECORD", getId(), e.getMessage());
				log(_msg, new BaseException(BasicCodeEnum.MSG_0019, e));
			}
		}
	}

	public String filter(RecordEntity entity) {
		if (record == null) {
			return entity.getMessage();
		}
		try {
			return record.filter(entity);
		} catch (Exception e) {
			return entity.toString();
		}
	}

	/**
	 * 增加监控日志信息
	 * 
	 * @param entity 日志信息
	 */
	public void push(RecordEntity entity) {
		if (loop.get()) {
			cache.offer(entity);
		}
	}

	@Override
	protected void log(String msg, BaseException e) {
		if (e != null) {
			System.err.println(msg);
			e.printStackTrace();
		} else {
			System.out.println(msg);
		}
	}

	/**
	 * 销毁当前线程
	 * 
	 * @param timeout 销毁等待时间
	 */
	@Override
	public void destroy(Duration timeout) {
		loop.set(false);

		if (record != null) {
			unloadPlugin(record, timeout);
			log(LocaleHelper.toLocaleText(TypeEnum.LOG, "RECORD.DESTROY", getId(), timeout), null);
		}
		if (thread != null) {
			if (timeout.isZero() || timeout.isZero()) {
				thread.shutdownNow();
			} else {
				thread.shutdown();
			}
		}
	}
}

package org.pizazz.tool;

import java.io.Serializable;
import java.time.Duration;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.pizazz.ICloseable;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.StringUtils;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;

/**
 * 双端队列组件<br>
 * 代码参考DataX
 * 
 * @author xlgp2171
 * @version 1.1.190219
 */
public class DoubleQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable, ICloseable {
	private static final long serialVersionUID = 8209223542093795745L;
	// 队列数据源A
	private Object[] readArray;
	// 队列数据源B
	private Object[] writeArray;
	// 读取队列锁
	private final ReentrantLock readLock;
	// 写入队列锁
	private final ReentrantLock writeLock;
	//
	private final Condition notFull;
	private final Condition awake;
	// 写队列 数据个数
	private volatile int writeCount;
	// 读队列数据个数
	private volatile int readCount;
	// 写队列序列值
	private int writeIndex;
	// 读队列序列值
	private int readIndex;
	// 是否无限等待
	private final boolean isInfinite;
	// 队列长度
	private final int capacity;

	public DoubleQueue(int capacity, boolean isInfinite) throws AssertException {
		AssertUtils.assertLimit("DoubleQueue", 1, capacity, 1, null);
		this.capacity = capacity;
		// 初始双重队列
		readArray = new Object[capacity];
		writeArray = new Object[capacity];
		// 初始化队列锁
		readLock = new ReentrantLock();
		writeLock = new ReentrantLock();
		// 初始化锁对象
		notFull = writeLock.newCondition();
		awake = writeLock.newCondition();
		this.isInfinite = isInfinite;
	}

	/**
	 * 插入队列
	 * 
	 * @param item 单个数据
	 */
	protected void insert(E item) {
		writeArray[writeIndex++] = item;
		++writeCount;
	}

	/**
	 * 取出数据
	 * 
	 * @return 单个数据
	 */
	@SuppressWarnings("unchecked")
	protected E extract() {
		E _tmp = (E) readArray[readIndex];
		readArray[readIndex++] = null;
		--readCount;
		return _tmp;
	}

	/**
	 * 
	 * @param timeout 超时时间
	 * @return
	 * @throws InterruptedException
	 */
	protected long transfer(long timeout) throws InterruptedException {
		writeLock.lock();
		try {
			if (writeCount <= 0) {
				if (isInfinite && timeout <= 0) {
					awake.await();
					return -1;
				} else {
					return awake.awaitNanos(timeout);
				}
			} else {
				Object[] _tmp = readArray;
				readArray = writeArray;
				writeArray = _tmp;
				readCount = writeCount;
				readIndex = 0;
				writeCount = 0;
				writeIndex = 0;
				notFull.signal();
				return 0;
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * 插入数据
	 * 
	 * @param e 单个数据
	 * @return 是否插入成功
	 */
	@Override
	public boolean offer(E e) {
		try {
			return offer(e, 0, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e1) {
			return false;
		}
	}

	/**
	 * 插入数据,可以等待一段时间
	 * 
	 * @param e 单个数据
	 * @param timeout 超时时间
	 * @param unit 时间单位
	 * @return 是否插入成功
	 */
	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		long _nanoTime = unit.toNanos(timeout);
		// 获取锁 否则等待锁释放
		writeLock.lockInterruptibly();
		try {
			while (true) {
				// 写队列数量小于写队列长度，则插入数据
				if (writeCount < capacity) {
					// 插入数据
					insert(e);
					// 通知快速读取数据
					awake.signal();
					return true;
				}
				// 如果超时，则插入失败
				if (_nanoTime <= 0) {
					return false;
				}
				// 等待数据读取完成激活或等待超时
				_nanoTime = notFull.awaitNanos(_nanoTime);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void put(E e) throws InterruptedException {
		offer(e, 0, TimeUnit.NANOSECONDS);
	}

	@Override
	public E poll() {
		try {
			return poll(0, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	/**
	 * 获取数据
	 * 
	 * @param timeout
	 * @param unit
	 */
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long _nanoTime = unit.toNanos(timeout);
		// 获取锁 否则等待锁释放
		readLock.lockInterruptibly();
		try {
			while (true) {
				if (readCount > 0) {
					return extract();
				}
				// 当等待超时时，返回null
				if (_nanoTime <= 0) {
					return null;
				}
				_nanoTime = transfer(_nanoTime);
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public E take() throws InterruptedException {
		return poll(0, TimeUnit.NANOSECONDS);
	}

	@Override
	public int remainingCapacity() {
		return capacity - writeCount;
	}

	@Override
	public int size() {
		return readCount;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		awake.signalAll();
		notFull.signalAll();

		if (writeLock.isLocked()) {
			writeLock.unlock();
		}
		if (readLock.isLocked()) {
			readLock.unlock();
		}
	}

	@Override
	public E peek() {
		throw new UnsupportedOperationException("peek");
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException("iterator");
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		throw new UnsupportedOperationException("drainTo");
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		throw new UnsupportedOperationException("drainTo");
	}

	@Override
	public String toString() {
		return StringUtils.join(new Integer[] { capacity, writeCount, writeIndex, readCount, readIndex }, ":");
	}
}

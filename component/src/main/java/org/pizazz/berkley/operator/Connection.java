package org.pizazz.berkley.operator;

import java.time.Duration;

import org.pizazz.ICloseable;
import org.pizazz.common.ArrayUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.exception.BaseException;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class Connection<E> implements Iterable<DataObject<E>>, ICloseable {
	private final StoredClassCatalog log;
	private final Database data;
	private final Database clazz;
	private final Class<E> type;
	private boolean autoCommit = true;

	public Connection(Database data, Database clazz, Class<E> type) {
		this.data = data;
		this.clazz = clazz;
		this.type = type;
		log = new StoredClassCatalog(clazz);
	}

	@SuppressWarnings({ "unchecked" })
	public OperationStatus put(byte[] key, E element) {
		DatabaseEntry _data = new DatabaseEntry();
		new SerialBinding<E>(log, (Class<E>) element.getClass()).objectToEntry(element, _data);
		try {
			return this.data.put(null, new DatabaseEntry(key), _data);
		} finally {
			if (autoCommit) {
				commit();
			}
		}
	}

	public E get(byte[] key) {
		DatabaseEntry _data = new DatabaseEntry();
		data.get(null, new DatabaseEntry(key), _data, LockMode.DEFAULT);

		if (ArrayUtils.isEmpty(_data.getData())) {
			return null;
		}
		// 根据存储的类信息还原数据
		return new SerialBinding<E>(log, type).entryToObject(_data);
	}

	public OperationStatus remove(byte[] key) {
		DatabaseEntry _key = new DatabaseEntry(key);
		OperationStatus _tmp = clazz.delete(null, _key);

		if (_tmp != OperationStatus.SUCCESS) {
			return _tmp;
		}
		try {
			return data.delete(null, _key);
		} finally {
			if (autoCommit) {
				commit();
			}
		}
	}

	public boolean containsKey(byte[] key) {
		try (Cursor _cursor = data.openCursor(null, null)) {
			return _cursor.getSearchKey(new DatabaseEntry(key), new DatabaseEntry(),
					LockMode.DEFAULT) == OperationStatus.SUCCESS;
		}
	}

	public int size() {
		try (Cursor _cursor = data.openCursor(null, null)) {
			return _cursor.getNext(new DatabaseEntry(), new DatabaseEntry(),
					LockMode.DEFAULT) == OperationStatus.SUCCESS ? _cursor.count() : 0;
		}
	}

	public Looper<E> iterator() {
		return new Looper<E>(data, log, type);
	}

	public void commit() {
		clazz.sync();
		data.sync();
	}

	public boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		IOUtils.close(data);
		IOUtils.close(clazz);
		IOUtils.close(log);
	}
}

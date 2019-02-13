package org.pizazz.berkley.operator;

import java.time.Duration;
import java.util.Iterator;

import org.pizazz.ICloseable;
import org.pizazz.common.IOUtils;
import org.pizazz.exception.BaseError;
import org.pizazz.exception.BaseException;
import org.pizazz.message.ErrorCodeEnum;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class Looper<E> implements Iterator<DataObject<E>>, ICloseable {

	private final Cursor cursor;
	private final EntryBinding<E> binding;
	private final DatabaseEntry key = new DatabaseEntry();
	private final DatabaseEntry data = new DatabaseEntry();

	public Looper(Database database, StoredClassCatalog log, Class<E> type) {
		cursor = database.openCursor(null, null);
		binding = new SerialBinding<E>(log, type);
	}

	@Override
	public boolean hasNext() {
		return cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS;
	}

	@Override
	public DataObject<E> next() {
		try {
			return new DataObject<E>(key.getData(), binding.entryToObject(data));
		} catch (BaseException e) {
			throw new BaseError(ErrorCodeEnum.ERR_0002, e);
		}
	}

	@Override
	public void remove() {
		cursor.delete();
	}

	@Override
	public void destroy(Duration timeout) throws BaseException {
		IOUtils.close(cursor);
	}
}

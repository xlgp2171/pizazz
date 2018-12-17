package org.pizazz.tool;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.pizazz.IMessageOutput;
import org.pizazz.IRunnable;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 文件夹监视组件
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class FolderWatcher implements IRunnable {

	private final WatchService service;
	private final IMessageOutput<WatchEvent<?>> watcher;

	public FolderWatcher(Path dir, IMessageOutput<WatchEvent<?>> watcher) throws BaseException {
		AssertUtils.assertNotNull("FolderWatcher", dir, watcher);
		this.watcher = watcher;
		try {
			service = FileSystems.getDefault().newWatchService();
			dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR.WATCHER", dir.toAbsolutePath(),
					e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
	}

	@Override
	public void run() {
		while (watcher.isEnable()) {
			WatchKey _key = null;
			try {
				_key = service.take();
			} catch (InterruptedException e) {
				watcher.throwException(e);
				break;
			}
			for (WatchEvent<?> _event : _key.pollEvents()) {
				try {
					watcher.write(_event);
				} catch (Exception e) {
					watcher.throwException(e);
				}
			}
			if (!_key.reset()) {
				break;
			}
		}
	}

	@Override
	public void enable() throws BaseException {
	}

	@Override
	public void destroy(int timeout) {
		IOUtils.close(service);
		IOUtils.close(watcher, timeout);
	}
}

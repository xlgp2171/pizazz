package org.pizazz2.tool;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;

import org.pizazz2.IMessageOutput;
import org.pizazz2.IRunnable;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.BaseException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 文件夹监视组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class FolderWatcher implements IRunnable {

	private final WatchService service;
	private final IMessageOutput<WatchEvent<?>> watcher;

	public FolderWatcher(Path dir, IMessageOutput<WatchEvent<?>> watcher) throws ValidateException, ToolException {
		ValidateUtils.notNull("FolderWatcher", dir, watcher);
		this.watcher = watcher;
		try {
			service = FileSystems.getDefault().newWatchService();
			dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR.WATCHER", dir.toAbsolutePath(),
					e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
		}
	}

	@Override
	public void activate() throws BaseException {
		while (watcher.isEnabled()) {
			WatchKey key;
			try {
				key = service.take();
			} catch (InterruptedException e) {
				watcher.throwException(e);
				break;
			}
			key.pollEvents().forEach(item -> {
				try {
					watcher.write(item);
				} catch (Exception e) {
					watcher.throwException(e);
				}
			});
			if (!key.reset()) {
				break;
			}
		}
	}

	@Override
	public void destroy(Duration timeout) {
		SystemUtils.close(service);
		SystemUtils.destroy(watcher, timeout);
	}
}

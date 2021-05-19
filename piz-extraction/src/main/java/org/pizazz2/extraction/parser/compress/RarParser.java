package org.pizazz2.extraction.parser.compress;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.*;
import org.pizazz2.IMessageOutput;
import org.pizazz2.PizContext;
import org.pizazz2.common.PathUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.tool.ShellFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RAR(application/x-rar-compressed)解析<br>
 * 无解析属性Metadata
 *
 * @author xlgp2171
 * @version 2.0.210501
 */
public class RarParser extends AbstractCompressParser {
	@Override
	public IConfig toConfig(TupleObject config) {
		return new Config(config);
	}

	@Override
	protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
			ValidateException, DetectionException {
		RARVersion version = getVersion(object.getData());

		if (version != null) {
			Config tmp = config.getTarget(Config.class);
			try {
				switch (version) {
					case OLD:
					case V4:
						doV4Uncompress(object, tmp);
						break;
					case V5:
						doV5Uncompress(object, tmp);
						break;
					default:
						break;
				}
			} catch (IOException | RarException | BaseException | InterruptedException | ExecutionException e) {
				super.throwException(object, config, e);
			}
		} else {
			LOGGER.warn("RAR VERSION NOT FOUND,id=" + object + ",type=" + object.getType() + ",length=" +
					(object.getData() == null ? "NaN" : object.getData().length));
			object.setStatus(ExtractObject.StatusEnum.INVALID);
		}
	}

	protected RARVersion getVersion(byte[] data) {
		// 验证需要前7位
		if (data.length < BaseBlock.BaseBlockSize) {
			return null;
		}
		data = Arrays.copyOfRange(data, 0, BaseBlock.BaseBlockSize);
		BaseBlock block = new BaseBlock(data);
		UnrarHeadertype type = block.getHeaderType();

		if (UnrarHeadertype.MarkHeader == type) {
			MarkHeader markHead = new MarkHeader(block);
			// 验证版本
			if (markHead.isSignature() || markHead.getVersion() == RARVersion.V5) {
				return markHead.getVersion();
			}
		}
		return null;
	}

	protected boolean v4Encrypted(byte[] data) throws IOException, RarException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(data);
			 Archive archive = new Archive(in)) {

			if (archive.isEncrypted()) {
				// 存在加密
				return true;
			}
			for (FileHeader item : archive) {
				// 若文件存在加密则加密
				if (!item.isDirectory() && item.isEncrypted()) {
					return true;
				}
			}
		}
		return false;
	}

	protected void doV4Uncompress(ExtractObject object, Config config) throws IOException,
			RarException {
		if (v4Encrypted(object.getData())) {
			object.setStatus(ExtractObject.StatusEnum.ENCRYPTION);
			return;
		}
		Path parent = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());

		try (InputStream in = new ByteArrayInputStream(object.getData());
			 Archive archive = new Archive(in, config.password())) {
			for (FileHeader item : archive) {
				Path path = Paths.get(item.getFileName());

				if (item.isDirectory()) {
					if (config.includeDirectory()) {
						// 存储基础文件夹
						super.addAttachment(object, StringUtils.EMPTY, (parent == null ? path : parent.resolve(path))
								.toString()).setStatus(ExtractObject.StatusEnum.EMPTY);
					}
				} else {
					super.addAttachment(object, path.getFileName().toString(), ExtractHelper.pathResolve(parent, path))
							.setData(item.getFileNameByteArray());
				}
			}
		}
	}

	protected void doV5Uncompress(ExtractObject object, Config config) throws BaseException,
			InterruptedException, ExecutionException {
		if (!PathUtils.isRegularFile(config.rarPath())) {
			LOGGER.warn("NOT SUPPORT RAR_V5,id=" + object);
			object.setStatus(ExtractObject.StatusEnum.UNSUPPORTED);
			return;
		}
		Path baseDirectory = super.getExtractConfig().getBaseDirectory();
		Path itemPath = baseDirectory.resolve(object.getId() + ".rar");
		Path itemDirectory = PathUtils.createDirectories(baseDirectory.resolve(object.getId()));
		// 创建临时解压文件文件夹
		PathUtils.createDirectories(itemDirectory);
		// 将数据拷贝到临时解压文件
		PathUtils.copyToPath(object.getData(), itemPath);
        // 批处理命令
		String command = config.rarPath().toString() + " x -p" + config.password() + " -y " + itemPath + " " +
                itemDirectory;

		if (executeCommand(command, Optional.ofNullable(PizContext.LOCAL_ENCODING).orElse(config.charset()))) {
			object.setStatus(ExtractObject.StatusEnum.ENCRYPTION);
		} else {
			loadAllFiles(object, itemDirectory, itemPath, config.includeDirectory());
		}
	}

	/**
	 * RAR V5解压<br>
	 * 调用异常信息(中文/英文)
	 * <li>"文件已损坏或密码错误。“/”Corrupt file or wrong password.“
	 * <li>"全部错误: 4"/"Total errors: 4"
	 * <li>"指定的密码不正确。"/"The specified password is incorrect."
	 * <li>"全部错误: 1"/"Total errors: 1"
	 *
	 * @param command 运行命令
	 *
	 * @return 是否加密
	 * @throws BaseException 运行命令异常
	 * @throws InterruptedException 操作系统运行命令异常
	 * @throws ExecutionException 操作系统运行命令异常
	 */
	private boolean executeCommand(String command, Charset charset)
			throws InterruptedException, ExecutionException, BaseException {
		final AtomicBoolean encrypted = new AtomicBoolean(Boolean.FALSE);
		IMessageOutput<String> output = message -> {
			if ((message.endsWith("密码错误。") || message.endsWith("密码不正确。")
					|| message.endsWith("wrong password.") || message.endsWith("password is incorrect."))) {
				encrypted.compareAndSet(Boolean.FALSE, Boolean.TRUE);
			}
		};
		ShellFactory.newInstance(command).charset(charset).execute(output).get();
		return encrypted.get();
	}

	private void loadAllFiles(ExtractObject object, Path itemDirectory, Path itemPath, boolean includeDirectory)
			throws UtilityException {
		Path[] paths = PathUtils.listPaths(itemDirectory, item -> !item.equals(itemDirectory), true);
		Path parent = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());

		for (Path item : paths) {
			if (PathUtils.isDirectory(item)) {
				if (includeDirectory) {
					// 存储基础文件夹
					super.addAttachment(object, StringUtils.EMPTY, (parent == null ? item.getFileName() :
							parent.resolve(item.getFileName())).toString()).setStatus(ExtractObject.StatusEnum.EMPTY);
				}
			} else {
				byte[] data;
				try {
					data = PathUtils.toByteArray(item);
				} catch (UtilityException e) {
					LOGGER.error(e.getMessage() + ",id=" + object.getId() + ",name=" + item.getFileName());
					object.setStatus(ExtractObject.StatusEnum.BROKEN);
					continue;
				}
				super.addAttachment(object, item.getFileName().toString(), ExtractHelper.pathResolve(parent,
						itemDirectory.relativize(item))).setData(data);
			}
		}
		try {
			Files.move(itemDirectory, Paths.get(itemDirectory.toString() + ".done"),
					StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			Files.move(itemPath, Paths.get(itemPath.toString() + ".done"), StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.warn("MOVE PATH ERROR,directory=" + itemDirectory + ",path=" + itemPath);
		}
	}

	@Override
	public String[] getType() {
		return new String[] { "application/x-rar-compressed" };
	}

	public static class Config extends AbstractCompressParser.Config {
		private final Path rarPath;

		public Config(TupleObject config) throws ValidateException {
			super(config);
			// rar执行文件路径
			String rarPath = TupleObjectHelper.getString(config, "rarPath", StringUtils.EMPTY);
			// 若配置中没有再从环境中获取
			if (StringUtils.isTrimEmpty(rarPath)) {
				rarPath = SystemUtils.getSystemProperty(PizContext.NAMING_SHORT + ".rar.path",
						StringUtils.EMPTY);
			}
			this.rarPath = Paths.get(rarPath).toAbsolutePath();
		}

		public Path rarPath() {
			return rarPath;
		}
	}
}

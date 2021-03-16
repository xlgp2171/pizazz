package org.pizazz2.common;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.pizazz2.PizContext;
import org.pizazz2.IMessageOutput;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 输入输出工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class IOUtils {

	public static String readInputStream(InputStream is) throws ValidateException, UtilityException {
		byte[] data = IOUtils.toByteArray(is);
		return new String(data, StandardCharsets.UTF_8);
	}

	public static List<String> readLine(InputStream in, Charset charset) {
		final List<String> tmp = new LinkedList<>();
		IOUtils.readLine(in, charset, tmp::add);
		return tmp;
	}

	public static void readLine(InputStream in, Charset charset, IMessageOutput<String> call) throws ValidateException {
		ValidateUtils.notNull("readLine", in);
		call = call == null ? IMessageOutput.EMPTY_STRING : call;
		charset = charset == null ? StandardCharsets.UTF_8 : charset;

		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in, charset))) {
			String line = buffer.readLine();

			while (line != null) {
				call.write(line);
				line = buffer.readLine();
			}
		} catch (IOException e) {
			call.throwException(e);
		} finally {
			SystemUtils.close(in);
			SystemUtils.close(call);
		}
	}

	public static InputStream getResourceAsStream(String resource) throws ValidateException, UtilityException {
		return IOUtils.getResourceAsStream(resource, null, null);
	}

	public static InputStream getResourceAsStream(String resource, ClassLoader loader) throws UtilityException {
		ValidateUtils.notEmpty(resource, "getResourceAsStream");

		if (loader == null) {
			loader = PizContext.CLASS_LOADER;
		}
		InputStream stream = loader.getResourceAsStream(resource);

		if (stream == null) {
			stream = IOUtils.getInputStream(Paths.get(resource));
		}
		return stream;
	}

	public static InputStream getResourceAsStream(String resource, Class<?> clazz, Thread current)
			throws ValidateException, UtilityException {
		ValidateUtils.notEmpty(resource, "getResourceAsStream");
		InputStream stream;
		ClassLoader loader = ClassUtils.getClassLoader(clazz, current);

		if (loader != null) {
			stream = loader.getResourceAsStream(resource);

			if (stream == null && clazz != null) {
				stream = clazz.getResourceAsStream(resource);
			}
		} else {
			stream = PizContext.class.getResourceAsStream(resource);

			if (stream == null) {
				stream = PizContext.CLASS_LOADER.getResourceAsStream(resource);
			}
		}
		if (stream == null) {
			stream = IOUtils.getInputStream(Paths.get(resource));
		}
		return stream;
	}

	public static InputStream getInputStream(Path path) throws ValidateException, UtilityException {
		ValidateUtils.notNull("getInputStream", path);
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
	}

	public static OutputStream getOutputStream(Path path) throws ValidateException, UtilityException {
		ValidateUtils.notNull("getOutputStream", path);
		try {
			return Files.newOutputStream(path);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(), e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
	}

	public static long copyLarge(InputStream input, OutputStream output, int size)
			throws ValidateException, UtilityException {
		ValidateUtils.notNull("copyLarge", input, output);
		byte[] buffer = new byte[size <= 0 ? 4096 : size];
		long count = 0L;
		int len;
		try {
			while (-1 != (len = input.read(buffer))) {
				output.write(buffer, 0, len);
				count += len;
			}
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		} finally {
			SystemUtils.close(input);
			SystemUtils.close(output);
		}
		return count;
	}

	public static long copyLarge(Reader reader, Writer writer, int size) throws ValidateException, UtilityException {
		ValidateUtils.notNull("copyLarge", reader, writer);
		char[] buffer = new char[size <= 0 ? 4096 : size];
		long count = 0L;
		int len;
		try {
			while (-1 != (len = reader.read(buffer))) {
				writer.write(buffer, 0, len);
				count += len;
			}
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		} finally {
			SystemUtils.close(reader);
			SystemUtils.close(writer);
		}
		return count;
	}

	public static int copy(InputStream input, OutputStream output) throws ValidateException, UtilityException {
		Long count = IOUtils.copyLarge(input, output, 4096);
		return count > Integer.MAX_VALUE ? -1 : count.intValue();
	}

	public static long copy(FileInputStream in, FileOutputStream out) throws ValidateException, UtilityException {
		ValidateUtils.notNull("copy", in, out);

		try (FileChannel tmp = in.getChannel(); FileChannel outF = out.getChannel()) {
			// 将fileChannelInput通道的数据，写入到fileChannelOutput通道
			return tmp.transferTo(0, tmp.size(), outF);
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		} finally {
			SystemUtils.close(in);
			SystemUtils.close(out);
		}
	}

	public static int copy(Reader reader, Writer writer) throws ValidateException, UtilityException {
		Long count = IOUtils.copyLarge(reader, writer, 4096);
		return count > Integer.MAX_VALUE ? -1 : count.intValue();
	}

	public static byte[] toByteArray(InputStream input) throws ValidateException, UtilityException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			IOUtils.copy(input, output);
			return output.toByteArray();
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.OUT", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
	}

	public static byte[] toByteArray(Reader reader) throws ValidateException, UtilityException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(output)) {
			IOUtils.copy(reader, writer);
			return output.toByteArray();
		} catch (IOException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.OUT", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
		}
	}
}

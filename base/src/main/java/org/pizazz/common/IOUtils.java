package org.pizazz.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.pizazz.Constant;
import org.pizazz.ICloseable;
import org.pizazz.IMessageOutput;
import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;

/**
 * 输入输出工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class IOUtils {

	public static String readInputStream(InputStream is, boolean close) throws BaseException {
		byte[] _data = toByteArray(is, close);
		return new String(_data, StandardCharsets.UTF_8);
	}

	public static List<String> readLine(InputStream in, Charset charset) {
		final List<String> _tmp = new LinkedList<String>();
		readLine(in, charset, new IMessageOutput<String>() {
			@Override
			public void write(String message) {
				_tmp.add(message);
			}
		});
		return _tmp;
	}

	public static void readLine(InputStream in, Charset charset, IMessageOutput<String> call) {
		call = call == null ? MessageOutputHelper.EMPTY_STRING : call;

		if (in == null) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "readLine", 1);
			call.throwException(new BaseException(BasicCodeEnum.MSG_0001, _msg));
		}
		charset = charset == null ? StandardCharsets.UTF_8 : charset;

		try (BufferedReader _buffer = new BufferedReader(new InputStreamReader(in, charset))) {
			String _line = _buffer.readLine();

			while (_line != null) {
				call.write(_line);
				_line = _buffer.readLine();
			}
		} catch (IOException e) {
			call.throwException(e);
		} finally {
			close(in);
			close(call);
		}
	}

	public static InputStream getResourceAsStream(String resource, Class<?> clazz, Thread current)
			throws BaseException {
		if (StringUtils.isTrimEmpty(resource)) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.ARGS.NULL", "getResourceAsStream", 1);
			throw new BaseException(BasicCodeEnum.MSG_0001, _msg);
		}
		InputStream _stream = null;
		ClassLoader _loader = ClassUtils.getClassLoader(clazz, current);

		if (_loader != null) {
			_stream = _loader.getResourceAsStream(resource);

			if (_stream == null && clazz != null) {
				_stream = clazz.getResourceAsStream(resource);
			}
		} else {
			_stream = Constant.class.getResourceAsStream(resource);

			if (_stream == null) {
				_stream = Constant.class.getClassLoader().getResourceAsStream(resource);
			}
		}
		if (_stream == null) {
			try {
				_stream = Files.newInputStream(Paths.get(resource));
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", resource, e.getMessage());
				throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
			}
		}
		return _stream;
	}

	public static long copyLarge(InputStream input, OutputStream output, int buffer) throws BaseException {
		if (buffer <= 0) {
			buffer = 4096;
		}
		AssertUtils.assertNotNull("copyLarge", input, output);
		byte[] _buffer = new byte[buffer];
		long _count = 0L;
		int _len = 0;
		try {
			while (-1 != (_len = input.read(_buffer))) {
				output.write(_buffer, 0, _len);
				_count += _len;
			}
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		} finally {
			close(input);
			close(output);
		}
		return _count;
	}

	public static long copyLarge(Reader input, Writer output, int buffer) throws BaseException {
		if (buffer <= 0) {
			buffer = 4096;
		}
		AssertUtils.assertNotNull("copyLarge", input, output);
		char[] _buffer = new char[buffer];
		long _count = 0L;
		int _len = 0;
		try {
			while (-1 != (_len = input.read(_buffer))) {
				output.write(_buffer, 0, _len);
				_count += _len;
			}
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		}
		return _count;
	}

	public static int copy(InputStream input, OutputStream output) throws BaseException {
		long _count = copyLarge(input, output, 4096);
		return _count > Integer.MAX_VALUE ? -1 : new Long(_count).intValue();
	}

	public static int copy(Reader input, Writer output) throws BaseException {
		long _count = copyLarge(input, output, 4096);
		return _count > Integer.MAX_VALUE ? -1 : new Long(_count).intValue();
	}

	public static byte[] toByteArray(InputStream input, boolean close) throws BaseException {
		try (ByteArrayOutputStream _output = new ByteArrayOutputStream()) {
			copy(input, _output);
			return _output.toByteArray();
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.OUT", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		} finally {
			if (close) {
				close(input);
			}
		}
	}

	public static byte[] toByteArray(Reader input, boolean close) throws BaseException {
		try (ByteArrayOutputStream _output = new ByteArrayOutputStream();
				OutputStreamWriter _writer = new OutputStreamWriter(_output)) {
			copy(input, _writer);
			return _output.toByteArray();
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.OUT", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0003, _msg, e);
		} finally {
			if (close) {
				close(input);
			}
		}
	}

	public static void close(ICloseable target, int timeout) {
		if (target != null) {
			if (timeout > 0) {
				try {
					target.destroy(timeout);
				} catch (BaseException e) {
				}
			} else {
				try {
					target.close();
				} catch (BaseException e) {
				}
			}
		}
	}

	public static void close(AutoCloseable target) {
		if (target != null) {
			try {
				target.close();
			} catch (Exception e) {
			}
		}
	}
}

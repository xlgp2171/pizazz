package org.pizazz.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.pizazz.common.ArrayUtils;
import org.pizazz.common.AssertUtils;
import org.pizazz.common.ClassUtils;
import org.pizazz.common.ConfigureHelper;
import org.pizazz.common.IOUtils;
import org.pizazz.common.LocaleHelper;
import org.pizazz.common.StringUtils;
import org.pizazz.common.TupleObjectHelper;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.AssertException;
import org.pizazz.exception.BaseException;
import org.pizazz.exception.ToolException;
import org.pizazz.exception.UtilityException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;
import org.pizazz.tool.ref.IHttpConfig;
import org.pizazz.tool.ref.ResponseObject;

/**
 * HTTP连接组件
 * 
 * @author xlgp2171
 * @version 1.1.190617
 */
public class PHttpConnection {
	private final URL url;

	public PHttpConnection(String url) throws ToolException {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.URL", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0024, _msg, e);
		}
	}

	public PHttpConnection(URL url) throws AssertException {
		AssertUtils.assertNotNull("PHttpConnection", url);
		this.url = url;
	}

	public HttpURLConnection connect() throws BaseException {
		return connect("GET", TupleObjectHelper.emptyObject(), null, null);
	}

	/**
	 * 获取链接对象
	 * 
	 * @param method 包括"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE",
	 *            "TRACE"
	 * @param headers 输入headers
	 * @param data 链接输入数据
	 * @param config 自定义配置
	 * @return
	 * @throws AssertException
	 * @throws UtilityException
	 * @throws ToolException
	 */
	public HttpURLConnection connect(String method, TupleObject headers, byte[] data, IHttpConfig config)
			throws AssertException, UtilityException, ToolException {
		HttpURLConnection _connection = createHttpConnection(method, config);

		for (String _item : headers.keySet()) {
			_connection.setRequestProperty(_item, StringUtils.of(headers.get(_item)));
		}
		if (!ArrayUtils.isEmpty(data)) {
			_connection.setDoOutput(true);
			OutputStream _out;
			try {
				_out = _connection.getOutputStream();
				_out.write(data);
				_out.flush();
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.OUTPUT", e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0003, _msg, e);
			}
		}
		try {
			_connection.connect();
		} catch (SocketTimeoutException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.TIMEOUT", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0025, _msg, e);
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0016, _msg, e);
		}
		return _connection;
	}

	public ResponseObject response(HttpURLConnection connection) throws ToolException {
		int _code = 0;
		try {
			_code = connection.getResponseCode();
		} catch (IOException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0016, _msg, e);
		}
		if (_code == HttpURLConnection.HTTP_OK) {
			Map<String, List<String>> _properties = connection.getRequestProperties();
			byte[] _data;

			try (InputStream _stream = connection.getInputStream()) {
				_data = IOUtils.toByteArray(_stream);
			} catch (IOException | AssertException | UtilityException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.INPUT", e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0003, _msg, e);
			}
			disconnect(connection);
			return new ResponseObject(_code, _data, _properties);
		} else {
			return new ResponseObject(_code, null, null);
		}
	}

	public void disconnect(HttpURLConnection connection) {
		connection.disconnect();
	}

	protected HttpURLConnection createHttpConnection(String method, IHttpConfig config)
			throws AssertException, UtilityException, ToolException {
		config = config == null ? new IHttpConfig() {
			@Override
			public void set(URL url, HttpURLConnection connection) {
				connection.setConnectTimeout(ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_HTTP_TIMEOUT", 30000));
			}
		} : config;
		HttpURLConnection _connection = null;

		if ("https".equalsIgnoreCase(url.getProtocol())) {
			HttpsURLConnection _httpsConn = null;
			try {
				_httpsConn = ClassUtils.cast(url.openConnection(), HttpsURLConnection.class);
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0016, _msg, e);
			}
			_httpsConn.setSSLSocketFactory(createSSLContext(config.getDefaultSSLProtocol()).getSocketFactory());
			_httpsConn.setHostnameVerifier(createHostnameVerifier());
			_connection = _httpsConn;
		} else {
			try {
				_connection = ClassUtils.cast(url.openConnection(), HttpURLConnection.class);
			} catch (IOException e) {
				String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
				throw new ToolException(BasicCodeEnum.MSG_0016, _msg, e);
			}
		}
		try {
			_connection.setRequestMethod(method);
		} catch (ProtocolException e) {
			try {
				_connection.setRequestMethod("GET");
			} catch (ProtocolException e1) {
			}
		}
		config.set(url, _connection);
		return _connection;
	}

	/**
	 * 构建SSL环境
	 * 
	 * @param contextProtocol 指定的协议，如：SSLv3
	 * @return
	 * @throws ToolException
	 */
	protected SSLContext createSSLContext(String protocol) throws ToolException {
		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager _manager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		try {
			SSLContext _tmp = SSLContext.getInstance(protocol);
			_tmp.init(null, new TrustManager[] { _manager }, null);
			return _tmp;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.SSL", protocol, e.getMessage());
			throw new ToolException(BasicCodeEnum.MSG_0008, _msg, e);
		}
	}

	protected HostnameVerifier createHostnameVerifier() {
		return new HostnameVerifier() {
			@Override
			public boolean verify(String s, SSLSession sslsession) {
				System.err.println(s);
				return true;
			}
		};
	}
}

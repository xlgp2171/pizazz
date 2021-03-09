package org.pizazz2.tool;

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
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.common.ClassUtils;
import org.pizazz2.helper.ConfigureHelper;
import org.pizazz2.common.IOUtils;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.common.StringUtils;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.pizazz2.tool.ref.IHttpConfig;
import org.pizazz2.tool.ref.ResponseObject;

/**
 * HTTP连接组件
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PHttpConnection {
    public static final String PROTOCOL_HTTPS = "https";
    private final URL url;

    public PHttpConnection(String url) throws ToolException {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.URL", url, e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0024, msg, e);
        }
    }

    public PHttpConnection(URL url) throws ValidateException {
        ValidateUtils.notNull("PHttpConnection", url);
        this.url = url;
    }

    public HttpURLConnection connect() throws ValidateException, ToolException {
        return connect("GET", TupleObjectHelper.emptyObject(), null);
    }

    public HttpURLConnection connect(String method, TupleObject headers, byte[] data) throws ValidateException, ToolException {
        return connect(method, headers, data, null);
    }

    /**
     * 获取链接对象
     *
     * @param method 包括"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
     * @param headers 输入headers
     * @param data 链接输入数据
     * @param config 自定义配置
     * @return 链接对象
     *
     * @throws ValidateException 参数验证异常
     * @throws ToolException 连接异常
     */
    public HttpURLConnection connect(String method, TupleObject headers, byte[] data, IHttpConfig config) throws ValidateException, ToolException {
        HttpURLConnection connection = createHttpConnection(method, config);

        for (String item : headers.keySet()) {
            connection.setRequestProperty(item, StringUtils.of(headers.get(item)));
        }
        if (!ArrayUtils.isEmpty(data)) {
            connection.setDoOutput(true);
            OutputStream out;
            try {
                out = connection.getOutputStream();
                out.write(data);
                out.flush();
            } catch (IOException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.OUTPUT", e.getMessage());
                throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
            }
        }
        try {
            connection.connect();
        } catch (SocketTimeoutException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.TIMEOUT", url, e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0025, msg, e);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0016, msg, e);
        }
        return connection;
    }

    public static ResponseObject response(HttpURLConnection connection) throws ToolException {
        return PHttpConnection.response(connection, HttpURLConnection.HTTP_OK);
    }

    public static ResponseObject response(HttpURLConnection connection, int httpStatus) throws ValidateException, ToolException {
        int code;
        try {
            code = connection.getResponseCode();
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", connection.getURL(), e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0016, msg, e);
        }
        if (code == httpStatus) {
            Map<String, List<String>> properties = connection.getHeaderFields();
            byte[] data;

            try (InputStream stream = connection.getInputStream()) {
                data = IOUtils.toByteArray(stream);
            } catch (IOException | ValidateException | UtilityException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.INPUT", e.getMessage());
                throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
            } finally {
                disconnect(connection);
            }
            return new ResponseObject(code, data, properties);
        } else {
            PHttpConnection.disconnect(connection);
            return new ResponseObject(code, null, null);
        }
    }

    public static void disconnect(HttpURLConnection connection) {
        connection.disconnect();
    }

    protected HttpURLConnection createHttpConnection(String method, IHttpConfig config) throws ValidateException, ToolException {
        config = config == null ? new DefaultHttpConfig() : config;
        HttpURLConnection connection;

        if (PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
            HttpsURLConnection httpsConn;
            try {
                httpsConn = ClassUtils.cast(url.openConnection(), HttpsURLConnection.class);
            } catch (IOException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
                throw new ToolException(BasicCodeEnum.MSG_0016, msg, e);
            }
            httpsConn.setSSLSocketFactory(createSSLContext(config.getDefaultSSLProtocol()).getSocketFactory());
            httpsConn.setHostnameVerifier(createHostnameVerifier());
            connection = httpsConn;
        } else {
            try {
                connection = ClassUtils.cast(url.openConnection(), HttpURLConnection.class);
            } catch (IOException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.CONNECTION", url, e.getMessage());
                throw new ToolException(BasicCodeEnum.MSG_0016, msg, e);
            }
        }
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e1) {
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e2) {
                // do nothing
            }
        }
        config.set(url, connection);
        return connection;
    }

    /**
     * 构建SSL环境
     *
     * @param protocol 指定的协议，如：SSLv3
     * @return SSL
     *
     * @throws ToolException SSL环境构建异常
     */
    protected SSLContext createSSLContext(String protocol) throws ToolException {
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager manager = new X509TrustManager() {
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
            SSLContext tmp = SSLContext.getInstance(protocol);
            tmp.init(null, new TrustManager[] { manager }, null);
            return tmp;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.HTTP.SSL", protocol, e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0008, msg, e);
        }
    }

    protected HostnameVerifier createHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    public URL getUrl() {
        return url;
    }

    /**
     * 为方便外部继承使用
     */
    public static class DefaultHttpConfig implements IHttpConfig {
        @Override
        public void set(URL url, HttpURLConnection connection) {
            // 连接超时
            connection.setConnectTimeout(ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_HTTP_TIMEOUT", 30000));
            // 读取超时
            connection.setReadTimeout(ConfigureHelper.getInt(TypeEnum.BASIC, "DEF_HTTP_TIMEOUT", 30000));
        }

        @Override
        public String getDefaultSSLProtocol() {
            return "SSLv3";
        }
    }
}

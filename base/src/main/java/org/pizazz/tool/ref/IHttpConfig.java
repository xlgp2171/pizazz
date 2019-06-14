package org.pizazz.tool.ref;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP连接设置
 * 
 * @author xlgp2171
 * @version 1.0.190614
 */
public interface IHttpConfig {

	public default void set(URL url, HttpURLConnection connection) {
	}

	public default String getDefaultSSLProtocol() {
		return "SSLv3";
	}
}

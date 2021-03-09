package org.pizazz2.tool.ref;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP连接设置
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public interface IHttpConfig {

	/**
	 * 设置HTTP配置
	 * @param url URL
	 * @param connection HTTP对象
	 */
	void set(URL url, HttpURLConnection connection);

	/**
	 * 获取默认的SSL协议
	 * @return SSL协议
	 */
	String getDefaultSSLProtocol();
}

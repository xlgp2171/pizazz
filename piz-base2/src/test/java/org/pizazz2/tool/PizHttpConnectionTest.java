package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.common.JSONUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.data.ResponseObject;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.TupleObjectHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PizHttpConnection测试
 *
 * @author xlgp2171
 * @version 2.1.211111
 */
public class PizHttpConnectionTest {

	private HttpURLConnection connect(String url) throws ToolException {
		HttpURLConnection tmp = null;
		try {
			tmp = new PizHttpConnection(url).connect();
		} catch (ToolException e) {
			if (!(e.getCause() instanceof ConnectException)) {
				throw e;
			}
		}
		return tmp;
	}

    @Test
    public void testConnect() throws IOException, UtilityException, ToolException {
		String url = "https://www.baidu.com";
		HttpURLConnection conn = connect(url);

        if (conn != null) {
            Assert.assertEquals(conn.getResponseCode(), 200);
            Assert.assertEquals(conn.getContentType(), "text/html");
            byte[] result = IOUtils.toByteArray(conn.getInputStream());
            Assert.assertEquals(new String(result).substring(0, 9), "<!DOCTYPE");
        }
    }

	@Test
	public void testResponse() throws ToolException {
		String url = "https://www.baidu.com";
		HttpURLConnection conn = connect(url);

		if (conn != null) {
			ResponseObject<List<String>, byte[]> response = PizHttpConnection.response(conn);
			Assert.assertEquals(response.getCode(), 200);
			Assert.assertEquals(new String(response.getResult()).substring(0, 9), "<!DOCTYPE");
		}
	}

	private void readData(Map<String, List<String>> target, String key, Path path) throws UtilityException {
		target.put(key, PathUtils.readLine(path, StandardCharsets.UTF_8));
	}

	private void readDataBySize(Map<String, List<String>> target, String key, Path path, int size)
			throws UtilityException {
		target.put(key, PathUtils.readLine(path, StandardCharsets.UTF_8).subList(0, size));
	}

	private void connection(String url, TupleObject request, String key) throws ToolException {
		String requestString = JSONUtils.toJSON(request);
		HttpURLConnection post = new PizHttpConnection(url)
				.connect("POST", TupleObjectHelper.emptyObject(), requestString.getBytes());
		ResponseObject<List<String>, byte[]> response = PizHttpConnection.response(post, -1);
		System.out.println(key + ": " + response.getCode());
		System.out.println(key + ": " + new String(response.getResult()));
	}

	private TupleObject newRequestData(long id) throws UtilityException {
		// 模型参数
		TupleObject model = TupleObjectHelper.newObject("id", id)
				.append("type", "TextCNN").append("path", "E:/MayDelete/raac-ml/" + id);
		// 数据参数
		Map<String, List<String>> data = new HashMap<>();
		readData(data, "1", Paths.get("E:\\MayDelete\\sohu\\sample\\CJ.txt"));
		readData(data, "0", Paths.get("E:\\MayDelete\\sohu\\sample\\WH.txt"));
		// 请求参数
		return TupleObjectHelper.newObject("data", data)
				.append("model", model);
	}

	@Test
	public void testRequest01() throws ToolException, UtilityException {
		// 用于测试raac-ml的train
		String url = "http://127.0.0.1:5001/train";

		ExecutorService service = Executors.newFixedThreadPool(3);
		TupleObject data1 = newRequestData(123454322L);
		TupleObject data2 = newRequestData(123454323L);
		service.submit(() -> {
			try {
				connection(url, data1, "1");
			} catch (ToolException e) {
				e.printStackTrace();
			}
		});
		service.submit(() -> {
			try {
				connection(url, data2, "2");
			} catch (ToolException e) {
				e.printStackTrace();
			}
		});
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		service.shutdown();
	}

	@Test
	public void testRequest02() throws ToolException, UtilityException {
		// 用于测试raac-ml的validate
		String url = "http://127.0.0.1:5001/validate";
		long id = 123454321L;
		TupleObject model = TupleObjectHelper.newObject("id", id)
				.append("type", "TextCNN").append("path", "E:/MayDelete/raac-ml/" + id);
		id = 123454322L;
		TupleObject current = TupleObjectHelper.newObject("id", id)
				.append("type", "TextCNN").append("path", "E:/MayDelete/raac-ml/" + id);
		// 数据参数
		Map<String, List<String>> data = new HashMap<>();
		readDataBySize(data, "1", Paths.get("E:\\MayDelete\\sohu\\sample\\CJ.txt"), 100);
		readDataBySize(data, "0", Paths.get("E:\\MayDelete\\sohu\\sample\\WH.txt"), 100);
		// 请求参数
		TupleObject request = TupleObjectHelper.newObject("data", data)
				.append("model", model).append("current", current);
		connection(url, request, "def");
	}

	@Test
	public void testRequest03() throws UtilityException, ToolException {
		// 用于测试raac-ml的predict
		String url = "http://127.0.0.1:5001/predict";
		long id = 123454322L;
		TupleObject model = TupleObjectHelper.newObject("id", id)
				.append("type", "TextCNN").append("path", "E:/MayDelete/raac-ml/" + id);
		Path path = Paths.get("E:\\MayDelete\\sohu\\sample\\CJ.txt");
		List<String> data = PathUtils.readLine(path, StandardCharsets.UTF_8).subList(0,10);
		// 请求参数
		TupleObject request = TupleObjectHelper.newObject("data", data)
				.append("model", model);
		connection(url, request, "def");
	}

	@Test
	public void testRequest04() throws ToolException {
		// 用于测试raac-ml的predict
		String url = "http://127.0.0.1:5001/delete";
		long id = 123454321L;
		TupleObject model = TupleObjectHelper.newObject("id", id)
				.append("type", "TextCNN").append("path", "E:/MayDelete/raac-ml/" + id);
		// 请求参数
		TupleObject request = TupleObjectHelper.newObject("model", model);
		connection(url, request, "def");
	}

	@Test
	public void testRequest98() throws ToolException {
		String url = "http://127.0.0.1:5001/get_models";
		HttpURLConnection post = new PizHttpConnection(url)
				.connect("POST", TupleObjectHelper.emptyObject(), "".getBytes());
		ResponseObject<List<String>, byte[]> response = PizHttpConnection.response(post, -1);
		System.out.println(response.getCode());
		System.out.println(new String(response.getResult()));
	}

	@Test
	public void testRequest99() throws ToolException {
		String url = "http://127.0.0.1:5001/ping";
		HttpURLConnection post = new PizHttpConnection(url)
				.connect("GET", TupleObjectHelper.emptyObject(), "".getBytes());
		ResponseObject<List<String>, byte[]> response = PizHttpConnection.response(post, -1);
		System.out.println(response.getCode());
		System.out.println(new String(response.getResult()));
	}
}

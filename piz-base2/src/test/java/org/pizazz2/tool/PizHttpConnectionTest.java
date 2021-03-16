package org.pizazz2.tool;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.common.IOUtils;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.tool.ref.ResponseObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;

/**
 * PizHttpConnection测试
 *
 * @author xlgp2171
 * @version 2.0.210201
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
			ResponseObject response = PizHttpConnection.response(conn);
			Assert.assertEquals(response.getCode(), 200);
			Assert.assertEquals(new String(response.getData()).substring(0, 9), "<!DOCTYPE");
		}
	}
}

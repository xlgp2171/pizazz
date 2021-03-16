package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PathUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class PathUtilsTest {

	@Test
	public void testToURIAndResolve() throws UtilityException {
		URI uri = PathUtils.toURI("file:/usr/local/mysql");
		URI result = PathUtils.resolve(uri, "bin");
		Assert.assertEquals(result.toString(),"file:/usr/local/mysql/bin");
	}

	@Test
	public void testCopyToTemp() throws UtilityException {
		String target = "piz";
		byte[] data = "中文".getBytes(StandardCharsets.UTF_8);
		Path path = PathUtils.copyToTemp(data, target);
		Assert.assertTrue(path.getFileName().toString().startsWith(target));
	}

	@Test
	public void testIsRegularFile() {
		boolean result = PathUtils.isRegularFile(Paths.get("/usr/local/pizazz.key"));
		Assert.assertFalse(result);
	}
}

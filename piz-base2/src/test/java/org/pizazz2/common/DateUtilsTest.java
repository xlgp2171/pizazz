package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.UtilityException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class DateUtilsTest {

	@Test
	public void testParseAndFormat() throws UtilityException {
		String target = "20210201 01:23:45";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
		LocalDateTime dateTime = DateUtils.parse(target, formatter);
		String result = DateUtils.format(dateTime, "yyyyMMdd HH:mm:ss", null);
		Assert.assertEquals(result, target);
	}

	@Test
	public void testAddDays() {
		int target = 31;
		LocalDateTime dateTime = LocalDateTime.now();
		LocalDateTime result = DateUtils.addDays(dateTime,  target, null);
		Assert.assertEquals(result, dateTime.plusDays(target));
	}
}

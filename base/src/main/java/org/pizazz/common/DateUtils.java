package org.pizazz.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.ref.TypeEnum;

/**
 * 时间日期工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class DateUtils {

	public static Date parse(String time, String pattern) throws BaseException {
		AssertUtils.assertNotNull("parse", time, pattern);
		try {
			return new SimpleDateFormat(pattern).parse(time);
		} catch (ParseException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.DATE.PARSE", time, pattern);
			throw new BaseException(BasicCodeEnum.MSG_0017, _msg, e);
		}
	}

	public static String format(Date date, String pattern) throws BaseException {
		AssertUtils.assertNotNull("format", date, pattern);
		return format(date, pattern, SystemUtils.LOCAL_LOCALE);
	}

	public static String format(Date date, String pattern, Locale locale) throws BaseException {
		AssertUtils.assertNotNull("format", date, pattern, locale);
		return new SimpleDateFormat(pattern, locale).format(date);
	}

	public static Date addDays(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_calendar.add(Calendar.DATE, amount);
		return _calendar.getTime();
	}

	public static Date addHours(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_calendar.add(Calendar.HOUR_OF_DAY, amount);
		return _calendar.getTime();
	}

	public static Date addMonths(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_calendar.add(Calendar.MONTH, amount);
		return _calendar.getTime();
	}

	public static Date addSeconds(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_calendar.add(Calendar.SECOND, amount);
		return _calendar.getTime();
	}

	public static Date addMinutes(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_calendar.add(Calendar.MINUTE, amount);
		return _calendar.getTime();
	}

	public static Date getDayOfWeek(Date date, int amount) {
		if (date == null) {
			date = new Date();
		}
		Calendar _calendar = Calendar.getInstance();
		_calendar.setTime(date);
		int _tmp = _calendar.get(Calendar.DAY_OF_WEEK) - 1;
		_calendar.add(Calendar.DATE, -(_tmp == 0 ? 7 : _tmp) + amount);
		return _calendar.getTime();
	}
}

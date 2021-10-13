package org.pizazz2.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import org.pizazz2.PizContext;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

/**
 * 时间日期工具
 * 
 * @author xlgp2171
 * @version 2.1.211008
 */
public class DateUtils {
	/**
	 * 默认时间格式，格式化后如"2021-07-01 09:30:00"
	 */
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	static ZoneId nullToDefault(ZoneId zoneId) {
		return zoneId == null ? ZoneId.systemDefault() : zoneId;
	}

	public static Date toDate(LocalDateTime dateTime, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("toDate", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return Date.from(dateTime.atZone(zoneId).toInstant());
	}

	public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("toLocalDateTime", date);
		zoneId = DateUtils.nullToDefault(zoneId);
		return LocalDateTime.ofInstant(date.toInstant(), zoneId);
	}

	public static LocalDateTime toLocalDateTime(long timestamp, ZoneId zoneId) throws ValidateException {
		zoneId = DateUtils.nullToDefault(zoneId);
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);
	}

	public static Date parse(String time, String pattern) throws ValidateException {
		ValidateUtils.notNull("parse", time, pattern);
		try {
			return new SimpleDateFormat(pattern).parse(time);
		} catch (ParseException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.DATE.PARSE", time, pattern);
			throw new ValidateException(BasicCodeEnum.MSG_0017, msg, e);
		}
	}

	public static LocalDateTime parse(String time, DateTimeFormatter formatter) throws ValidateException {
		ValidateUtils.notNull("parse", time, formatter);
		try {
			return LocalDateTime.parse(time, formatter);
		} catch (DateTimeParseException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.DATE.PARSE", time, formatter.toString());
			throw new ValidateException(BasicCodeEnum.MSG_0017, msg, e);
		}
	}

	public static String format(long timestamp, String pattern, ZoneId zoneId) throws ValidateException {
		zoneId = DateUtils.nullToDefault(zoneId);
		return DateUtils.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId), pattern, zoneId);
	}

	public static String format(long timestamp, String pattern) {
		return DateUtils.format(timestamp, pattern, ZoneId.systemDefault());
	}

	public static String format(LocalDateTime dateTime, String pattern, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("format", dateTime, pattern);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

		if (zoneId != null) {
			return dateTime.atZone(zoneId).format(formatter);
		}
		return dateTime.format(formatter);
	}

	public static String format(Date date, String pattern) throws ValidateException {
		ValidateUtils.notNull("format", date, pattern);
		return DateUtils.format(date, pattern, PizContext.LOCAL_LOCALE);
	}

	public static String format(Date date, String pattern, Locale locale) throws ValidateException {
		ValidateUtils.notNull("format", date, pattern, locale);
		return new SimpleDateFormat(pattern, locale).format(date);
	}

	public static LocalDateTime addDays(LocalDateTime dateTime, int amount, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("addDays", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return dateTime.atZone(zoneId).plusDays(amount).toLocalDateTime();
	}

	public static Date addDays(Date date, int amount) throws ValidateException {
		ValidateUtils.notNull("addDays", date);
		return Date.from(Instant.ofEpochMilli(date.getTime()).plus(amount, ChronoUnit.DAYS));
	}

	public static LocalDateTime addHours(LocalDateTime dateTime, int amount, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("addHours", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return dateTime.atZone(zoneId).plusHours(amount).toLocalDateTime();
	}

	public static Date addHours(Date date, int amount) throws ValidateException {
		ValidateUtils.notNull("addHours", date);
		return Date.from(Instant.ofEpochMilli(date.getTime()).plus(amount, ChronoUnit.HOURS));
	}

	public static LocalDateTime addMonths(LocalDateTime dateTime, int amount, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("addMonths", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return dateTime.atZone(zoneId).plusMonths(amount).toLocalDateTime();
	}

	public static Date addMonths(Date date, int amount) throws ValidateException {
		ValidateUtils.notNull("addMonths", date);
		return Date.from(Instant.ofEpochMilli(date.getTime()).plus(amount, ChronoUnit.MONTHS));
	}

	public static LocalDateTime addSeconds(LocalDateTime dateTime, int amount, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("addSeconds", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return dateTime.atZone(zoneId).plusSeconds(amount).toLocalDateTime();
	}

	public static Date addSeconds(Date date, int amount) throws ValidateException {
		ValidateUtils.notNull("addSeconds", date);
		return Date.from(Instant.ofEpochMilli(date.getTime()).plus(amount, ChronoUnit.SECONDS));
	}

	public static LocalDateTime addMinutes(LocalDateTime dateTime, int amount, ZoneId zoneId) throws ValidateException {
		ValidateUtils.notNull("addMinutes", dateTime);
		zoneId = DateUtils.nullToDefault(zoneId);
		return dateTime.atZone(zoneId).plusMinutes(amount).toLocalDateTime();
	}

	public static Date addMinutes(Date date, int amount) throws ValidateException {
		ValidateUtils.notNull("addMinutes", date);
		return Date.from(Instant.ofEpochMilli(date.getTime()).plus(amount, ChronoUnit.MINUTES));
	}
}

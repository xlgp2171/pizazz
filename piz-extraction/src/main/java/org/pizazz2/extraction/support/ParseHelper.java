package org.pizazz2.extraction.support;

import org.pizazz2.PizContext;
import org.pizazz2.common.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseHelper {
    static final Pattern XML_CHARSET_PATTERN = Pattern.compile("charset=(\"|)(?<charset>[\\w\\-]+)\\1",
            Pattern.CASE_INSENSITIVE);

    static final TimeZone MIDDAY = TimeZone.getTimeZone("GMT-12:00");
    static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    /**
     * TIKA-1970 Mac Mail's format
     */
    static final Pattern GENERAL_TIME_ZONE_NO_MINUTES_PATTERN = Pattern.compile("(?:UTC|GMT)([+-])(\\d?\\d)\\Z");
    /**
     * find a time ending in am/pm without a space: 10:30am and use this pattern to insert space: 10:30 am
     */
    static final Pattern AM_PM = Pattern.compile("(?i)(\\d)([ap]m)\\b");
    /**
     * @link org.apache.tika.parser.mail.MailContentHandler
     */
    static final DateFormat[] ALTERNATE_DATE_FORMATS = new DateFormat[] {
            //note that the string is "cleaned" before processing:
            //1) condense multiple whitespace to single space
            //2) trim()
            //3) strip out commas
            //4) insert space before am/pm
            //May 16 2016 1:32am
            ParseHelper.createDateFormat("MMM dd yy hh:mm a", null),
            //this is a standard pattern handled by mime4j;
            //but mime4j fails with leading whitespace
            ParseHelper.createDateFormat("EEE d MMM yy HH:mm:ss Z", UTC),
            ParseHelper.createDateFormat("EEE d MMM yy HH:mm:ss z", UTC),
            // no timezone
            ParseHelper.createDateFormat("EEE d MMM yy HH:mm:ss", null),
            // Sunday, May 15 2016 1:32 PM
            ParseHelper.createDateFormat("EEEEE MMM d yy hh:mm a", null),
            //16 May 2016 at 09:30:32  GMT+1 (Mac Mail TIKA-1970)
            // UTC/Zulu
            ParseHelper.createDateFormat("d MMM yy 'at' HH:mm:ss z", UTC),
            ParseHelper.createDateFormat("yy-MM-dd HH:mm:ss", null),
            ParseHelper.createDateFormat("MM/dd/yy hh:mm a", null, false),
            //now dates without times
            ParseHelper.createDateFormat("MMM d yy", MIDDAY, false),
            ParseHelper.createDateFormat("EEE d MMM yy", MIDDAY, false),
            ParseHelper.createDateFormat("d MMM yy", MIDDAY, false),
            ParseHelper.createDateFormat("yy/MM/dd", MIDDAY, false),
            ParseHelper.createDateFormat("MM/dd/yy", MIDDAY, false)
    };

    public static DateFormat createDateFormat(String format, TimeZone timezone) {
        return ParseHelper.createDateFormat(format, timezone, true);
    }

    private static DateFormat createDateFormat(String format, TimeZone timezone, boolean isLenient) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, new DateFormatSymbols(Locale.US));
        if (timezone != null) {
            sdf.setTimeZone(timezone);
        }
        sdf.setLenient(isLenient);
        return sdf;
    }

    public static Charset findCharset(String xml) {
        if (!StringUtils.isEmpty(xml)) {
            Matcher matcher = XML_CHARSET_PATTERN.matcher(xml);

            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group("charset"));
                } catch (Exception e) {
                    // fallback on default encoding
                }
            }
        }
        return null;
    }

    public static String convert(String target, String charsetName) {
        if (!StringUtils.isEmpty(charsetName) && Charset.isSupported(charsetName)) {
            return convert(target, Charset.forName(charsetName));
        }
        return target;
    }

    public static String convert(String target, Charset charset) {
        String tmp = StringUtils.nullToEmpty(target);

        if (charset != null && !PizContext.LOCAL_ENCODING.equals(charset)) {
            tmp = new String(tmp.getBytes(StandardCharsets.ISO_8859_1), charset);
        }
        return tmp;
    }

    /**
     * 尝试其它日期时间格式
     * @param text 日期内容
     * @return 日期对象
     */
    public static synchronized Date tryOtherDateFormats(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        text = text.replaceAll("\\s+", " ").trim();
        //strip out commas
        text = text.replaceAll(",", "");
        Matcher matcher = GENERAL_TIME_ZONE_NO_MINUTES_PATTERN.matcher(text);

        if (matcher.find()) {
            text = matcher.replaceFirst("GMT$1$2:00");
        }
        matcher = AM_PM.matcher(text);

        if (matcher.find()) {
            text = matcher.replaceFirst("$1 $2");
        }
        for (DateFormat format : ALTERNATE_DATE_FORMATS) {
            try {
                return format.parse(text);
            } catch (ParseException e) {
                // do nothing
            }
        }
        return null;
    }
}

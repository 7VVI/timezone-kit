package com.tzkit.utils;

import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.FastDateFormat;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.*;

/**
 * 多格式日期解析工具类
 */
public final class DateUtils {

    private DateUtils() {}

    /**
     * 自动解析多种格式的日期字符串
     * @param dateStr 日期字符串
     * @param timeZone 时区
     * @return Date对象
     */
    public static Date parse(CharSequence dateStr, TimeZone timeZone) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        String str = dateStr.toString().trim();
        // 去掉中文日期中的"日"和"秒"
        str = StrUtil.removeAll(str, '日', '秒');
        int length = str.length();

        if (NumberUtil.isNumber(str)) {
            // 纯数字形式
            if (length == PURE_DATETIME_PATTERN.length()) {
                return DateUtil.parse(str, FastDateFormat.getInstance(PURE_DATETIME_PATTERN, timeZone));
            } else if (length == PURE_DATETIME_MS_PATTERN.length()) {
                return DateUtil.parse(str, FastDateFormat.getInstance(PURE_DATETIME_MS_PATTERN, timeZone));
            } else if (length == PURE_DATE_PATTERN.length()) {
                return DateUtil.parse(str, FastDateFormat.getInstance(PURE_DATE_PATTERN, timeZone));
            } else if (length == PURE_TIME_PATTERN.length()) {
                return DateUtil.parse(str, FastDateFormat.getInstance(PURE_TIME_PATTERN, timeZone));
            } else if (length == 13) {
                // 时间戳
                return new Date(NumberUtil.parseLong(str));
            }
        } else if (ReUtil.isMatch(PatternPool.TIME, str)) {
            // HH:mm:ss 或者 HH:mm 时间格式
            return DateUtil.parseTimeToday(str);
        }

        // 标准日期格式
        str = normalize(str);
        if (ReUtil.isMatch(DatePattern.REGEX_NORM, str)) {
            final int colonCount = StrUtil.count(str, CharUtil.COLON);
            switch (colonCount) {
                case 0:
                    return DateUtil.parse(str, FastDateFormat.getInstance(NORM_DATE_PATTERN, timeZone));
                case 1:
                    return DateUtil.parse(str, FastDateFormat.getInstance(NORM_DATETIME_MINUTE_PATTERN, timeZone));
                case 2:
                    final int dotIndex = StrUtil.indexOf(str, CharUtil.DOT);
                    if (dotIndex > 0) {
                        int totalLength = str.length();
                        if (totalLength - dotIndex > 4) {
                            str = StrUtil.subPre(str, dotIndex + 4);
                        }
                        return DateUtil.parse(str, FastDateFormat.getInstance(NORM_DATETIME_MS_PATTERN, timeZone));
                    }
                    return DateUtil.parse(str, FastDateFormat.getInstance(NORM_DATETIME_PATTERN, timeZone));
            }
        }

        throw new DateException("No format fit for date String: {}", dateStr);
    }

    /**
     * 自动解析，使用默认时区(Asia/Shanghai)
     */
    public static Date parse(CharSequence dateStr) {
        return parse(dateStr, TimeZone.getTimeZone("Asia/Shanghai"));
    }

    private static String normalize(CharSequence dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            return StrUtil.str(dateStr);
        }

        final List<String> parts = StrUtil.splitTrim(dateStr, ' ');
        final int size = parts.size();
        if (size < 1 || size > 2) {
            return StrUtil.str(dateStr);
        }

        final StringBuilder builder = StrUtil.builder();

        // 日期部分
        String datePart = parts.get(0).replaceAll("[/.年月]", "-");
        datePart = StrUtil.removeSuffix(datePart, "日");
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(' ');
            String timePart = parts.get(1).replaceAll("[时分秒]", ":");
            timePart = StrUtil.removeSuffix(timePart, ":");
            timePart = timePart.replace(',', '.');
            builder.append(timePart);
        }

        return builder.toString();
    }
}

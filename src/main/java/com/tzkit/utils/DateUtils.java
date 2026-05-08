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
import com.tzkit.context.TimeZoneContext;
import com.tzkit.context.TimeZoneContextHolder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.*;

/**
 * 统一的时间工具类
 * 包含：多格式解析、时区转换、格式化、当前时间获取
 */
public final class DateUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN, Locale.ENGLISH);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Asia/Shanghai");

    // LocalDateTime支持的多种格式
    private static final DateTimeFormatter[] LDT_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE
    };

    private DateUtils() {}

    // ===== 时区获取 =====

    /**
     * 获取用户时区，未设置时返回默认时区 Asia/Shanghai
     */
    public static TimeZone getTimeZone() {
        TimeZone tz = TimeZoneContext.get();
        return tz != null ? tz : DEFAULT_TIMEZONE;
    }

    /**
     * 获取用户ZoneId，未设置时返回默认 ZoneId Asia/Shanghai
     */
    public static ZoneId getZoneId() {
        ZoneId zone = TimeZoneContext.getZoneId();
        return zone != null ? zone : DEFAULT_ZONE;
    }

    /**
     * 获取用户时区的ZoneOffset
     */
    public static ZoneOffset getZoneOffset() {
        return getZoneId().getRules().getOffset(Instant.now());
    }

    /**
     * 获取服务器时区（后端数据存储/处理的基准时区）
     */
    public static TimeZone getServerTimeZone() {
        return TimeZoneContextHolder.getServerTimeZone();
    }

    /**
     * 获取服务器ZoneId（后端数据存储/处理的基准ZoneId）
     */
    public static ZoneId getServerZoneId() {
        return TimeZoneContextHolder.getServerZoneId();
    }

    /**
     * 获取UTC时区
     */
    public static TimeZone getUtcTimeZone() {
        return TimeZone.getTimeZone("UTC");
    }

    // ===== 当前时间 =====

    /**
     * 获取用户时区的当前LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(getZoneId());
    }

    /**
     * 获取用户时区的当前LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now(getZoneId());
    }

    /**
     * 获取服务器当前LocalDateTime（使用服务器配置的时区）
     */
    public static LocalDateTime nowServer() {
        return LocalDateTime.now(getServerZoneId());
    }

    /**
     * 获取UTC当前LocalDateTime
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * 获取当前Instant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }

    // ===== 多格式解析 =====

    /**
     * 自动解析多种格式的日期字符串，返回java.util.Date
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
        return parse(dateStr, getTimeZone());
    }

    /**
     * 解析LocalDateTime，支持多种格式
     */
    public static LocalDateTime parseLdt(CharSequence text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String str = text.toString().trim();
        for (DateTimeFormatter formatter : LDT_FORMATTERS) {
            try {
                if (str.length() <= 10) {
                    return LocalDateTime.parse(str + " 00:00:00", LDT_FORMATTERS[0]);
                }
                return LocalDateTime.parse(str, formatter);
            } catch (Exception ignored) {}
        }
        throw new DateException("No format fit for date String: {}", text);
    }

    /**
     * 使用指定格式解析LocalDateTime
     */
    public static LocalDateTime parse(String text, String pattern) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        try {
            return LocalDateTime.parse(text, formatter);
        } catch (Exception e) {
            // Try parsing as LocalDate then convert to LocalDateTime
            try {
                LocalDate date = LocalDate.parse(text, formatter);
                return date.atStartOfDay();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to parse date: " + text, ex);
            }
        }
    }

    /**
     * 标准化日期字符串
     */
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

    // ===== 时区转换 =====

    /**
     * 将服务器时区 LocalDateTime 转换为用户时区
     */
    public static LocalDateTime toUserZone(LocalDateTime serverTime) {
        if (serverTime == null) {
            throw new IllegalArgumentException("serverTime must not be null");
        }
        return serverTime.atZone(getServerZoneId())
            .withZoneSameInstant(getZoneId())
            .toLocalDateTime();
    }

    /**
     * 将用户时区 LocalDateTime 转换为服务器时区
     */
    public static LocalDateTime toServerZone(LocalDateTime userTime) {
        if (userTime == null) {
            throw new IllegalArgumentException("userTime must not be null");
        }
        return userTime.atZone(getZoneId())
            .withZoneSameInstant(getServerZoneId())
            .toLocalDateTime();
    }

    /**
     * 将UTC LocalDateTime转换为用户时区
     * @deprecated 使用 {@link #toUserZone(LocalDateTime)} 代替，支持可配置的服务器时区
     */
    @Deprecated
    public static LocalDateTime toUserZoneUtc(LocalDateTime utcTime) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        return utcTime.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(getZoneId())
            .toLocalDateTime();
    }

    /**
     * 将用户时区LocalDateTime转换为UTC
     * @deprecated 使用 {@link #toServerZone(LocalDateTime)} 代替，支持可配置的服务器时区
     */
    @Deprecated
    public static LocalDateTime toUtc(LocalDateTime userTime) {
        if (userTime == null) {
            throw new IllegalArgumentException("userTime must not be null");
        }
        return userTime.atZone(getZoneId())
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    /**
     * Date对象时区转换（Date本身是时区无关的，直接返回）
     */
    public static Date toUserZone(Date utcDate) {
        return utcDate; // Date is timezone-agnostic
    }

    public static Date toUtc(Date userDate) {
        return userDate; // Date is timezone-agnostic
    }

    /**
     * 将Instant转换为用户时区的LocalDateTime
     */
    public static LocalDateTime toUserZone(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("instant must not be null");
        }
        return instant.atZone(getZoneId()).toLocalDateTime();
    }

    /**
     * 在指定时区之间转换LocalDateTime
     */
    public static LocalDateTime convert(LocalDateTime time, ZoneId from, ZoneId to) {
        if (time == null) {
            throw new IllegalArgumentException("time must not be null");
        }
        if (from == null) {
            throw new IllegalArgumentException("from zone must not be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to zone must not be null");
        }
        return time.atZone(from)
            .withZoneSameInstant(to)
            .toLocalDateTime();
    }

    // ===== 格式化 =====

    /**
     * 格式化UTC LocalDateTime为用户时区字符串（默认格式 yyyy-MM-dd HH:mm:ss）
     */
    public static String format(LocalDateTime utcTime) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        LocalDateTime userTime = toUserZone(utcTime);
        return DEFAULT_FORMATTER.format(userTime);
    }

    /**
     * 格式化UTC LocalDateTime为用户时区字符串（指定格式）
     */
    public static String format(LocalDateTime utcTime, String pattern) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        LocalDateTime userTime = toUserZone(utcTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        return formatter.format(userTime);
    }

    /**
     * 格式化UTC LocalDateTime为指定时区字符串（指定格式）
     */
    public static String format(LocalDateTime utcTime, String pattern, ZoneId zoneId) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("zoneId must not be null");
        }
        LocalDateTime targetTime = utcTime.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(zoneId)
            .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        return formatter.format(targetTime);
    }

    /**
     * 格式化Date为字符串（使用用户时区）
     */
    public static String format(Date date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern, Locale.ENGLISH);
        sdf.setTimeZone(getTimeZone());
        return sdf.format(date);
    }

    /**
     * 格式化Date为字符串（默认格式 yyyy-MM-dd HH:mm:ss）
     */
    public static String format(Date date) {
        return format(date, DEFAULT_PATTERN);
    }
}

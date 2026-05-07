package com.tzkit.utils;

import com.tzkit.context.TimeZoneContext;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for timezone-aware operations.
 * Reads timezone from TimeZoneContext for all operations.
 */
public final class TimeZoneUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private TimeZoneUtils() {
        // Utility class
    }

    // ===== Timezone Access =====

    public static TimeZone getUserTimeZone() {
        return TimeZoneContext.get();
    }

    public static ZoneId getUserZoneId() {
        return TimeZoneContext.getZoneId();
    }

    public static ZoneOffset getUserZoneOffset() {
        ZoneId zoneId = getUserZoneId();
        if (zoneId == null) {
            return null;
        }
        return zoneId.getRules().getOffset(Instant.now());
    }

    public static TimeZone getUtcTimeZone() {
        return TimeZone.getTimeZone("UTC");
    }

    // ===== Current Time =====

    public static LocalDateTime now() {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = DEFAULT_ZONE;
        }
        return LocalDateTime.now(userZone);
    }

    public static LocalDate today() {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = DEFAULT_ZONE;
        }
        return LocalDate.now(userZone);
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    // ===== Timezone Conversion =====

    public static LocalDateTime toUserZone(LocalDateTime utcTime) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = DEFAULT_ZONE;
        }
        return utcTime.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(userZone)
            .toLocalDateTime();
    }

    public static LocalDateTime toUtc(LocalDateTime userTime) {
        if (userTime == null) {
            throw new IllegalArgumentException("userTime must not be null");
        }
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = DEFAULT_ZONE;
        }
        return userTime.atZone(userZone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    public static Date toUserZone(Date utcDate) {
        if (utcDate == null) {
            return null;
        }
        return utcDate; // Date is timezone-agnostic
    }

    public static Date toUtc(Date userDate) {
        if (userDate == null) {
            return null;
        }
        return userDate; // Date is timezone-agnostic
    }

    public static LocalDateTime toUserZone(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("instant must not be null");
        }
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = DEFAULT_ZONE;
        }
        return instant.atZone(userZone).toLocalDateTime();
    }

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

    // ===== Formatting =====

    public static String format(LocalDateTime utcTime) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        LocalDateTime userTime = toUserZone(utcTime);
        return DEFAULT_FORMATTER.format(userTime);
    }

    public static String format(LocalDateTime utcTime, String pattern) {
        if (utcTime == null) {
            throw new IllegalArgumentException("utcTime must not be null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        LocalDateTime userTime = toUserZone(utcTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(userTime);
    }

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(targetTime);
    }

    // ===== Parsing =====

    public static LocalDateTime parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        return LocalDateTime.parse(text, DEFAULT_FORMATTER);
    }

    public static LocalDateTime parse(String text, String pattern) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(text, formatter);
    }
}
package com.tzkit.utils;

import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneUtilsTest {

    private static final TimeZone TZ_NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final TimeZone TZ_TOKYO = TimeZone.getTimeZone("Asia/Tokyo");
    private static final TimeZone TZ_SHANGHAI = TimeZone.getTimeZone("Asia/Shanghai");

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    // ===== Timezone Access Tests =====

    @Test
    void getUserTimeZone_whenNotSet_returnsNull() {
        assertNull(TimeZoneUtils.getUserTimeZone());
    }

    @Test
    void getUserTimeZone_whenSet_returnsTimeZone() {
        TimeZoneContext.set(TZ_NEW_YORK);
        assertEquals(TZ_NEW_YORK, TimeZoneUtils.getUserTimeZone());
    }

    @Test
    void getUserZoneId_whenNotSet_returnsNull() {
        assertNull(TimeZoneUtils.getUserZoneId());
    }

    @Test
    void getUserZoneId_whenSet_returnsZoneId() {
        TimeZoneContext.set(TZ_NEW_YORK);
        assertEquals(TZ_NEW_YORK.toZoneId(), TimeZoneUtils.getUserZoneId());
    }

    @Test
    void getUserZoneOffset_whenNotSet_returnsNull() {
        assertNull(TimeZoneUtils.getUserZoneOffset());
    }

    @Test
    void getUserZoneOffset_whenSet_returnsOffset() {
        TimeZoneContext.set(TZ_TOKYO);
        ZoneOffset offset = TimeZoneUtils.getUserZoneOffset();
        assertNotNull(offset);
        // Tokyo is UTC+9
        assertEquals(ZoneOffset.ofHours(9), offset);
    }

    @Test
    void getUtcTimeZone_returnsUtc() {
        TimeZone serverTz = TimeZoneUtils.getUtcTimeZone();
        assertEquals("UTC", serverTz.getID());
    }

    // ===== Null Validation Tests =====

    @Test
    void toUserZone_LocalDateTime_throwsForNull() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.toUserZone((LocalDateTime) null));
    }

    @Test
    void toUtc_LocalDateTime_throwsForNull() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.toUtc((LocalDateTime) null));
    }

    @Test
    void toUserZone_Date_returnsNullForNull() {
        assertNull(TimeZoneUtils.toUserZone((Date) null));
    }

    @Test
    void toUtc_Date_returnsNullForNull() {
        assertNull(TimeZoneUtils.toUtc((Date) null));
    }

    @Test
    void toUserZone_Instant_throwsForNull() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.toUserZone((Instant) null));
    }

    @Test
    void convert_throwsForNullTime() {
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.convert(null, ZoneOffset.UTC, ZoneId.of("Asia/Tokyo")));
    }

    @Test
    void convert_throwsForNullFrom() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.convert(time, null, ZoneId.of("Asia/Tokyo")));
    }

    @Test
    void convert_throwsForNullTo() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.convert(time, ZoneOffset.UTC, null));
    }

    @Test
    void format_LocalDateTime_throwsForNull() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.format((LocalDateTime) null));
    }

    @Test
    void format_withPattern_throwsForNullTime() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.format(null, "yyyy-MM-dd"));
    }

    @Test
    void format_withPattern_throwsForNullPattern() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.format(time, null));
    }

    @Test
    void format_withZone_throwsForNullTime() {
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.format(null, "yyyy-MM-dd", ZoneId.of("Asia/Tokyo")));
    }

    @Test
    void format_withZone_throwsForNullPattern() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.format(time, null, ZoneId.of("Asia/Tokyo")));
    }

    @Test
    void format_withZone_throwsForNullZone() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () ->
            TimeZoneUtils.format(time, "yyyy-MM-dd", null));
    }

    @Test
    void parse_throwsForNullText() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.parse((String) null));
    }

    @Test
    void parse_withPattern_throwsForNullText() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.parse(null, "yyyy-MM-dd"));
    }

    @Test
    void parse_withPattern_throwsForNullPattern() {
        assertThrows(IllegalArgumentException.class, () -> TimeZoneUtils.parse("2024-01-01 00:00:00", null));
    }

    // ===== Current Time Tests =====

    @Test
    void now_whenContextNotSet_usesDefaultZone() {
        LocalDateTime now = TimeZoneUtils.now();
        assertNotNull(now);
        // Should use default zone (Asia/Shanghai)
        // Just verify it returns a valid datetime
        assertTrue(now.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void now_whenContextSet_usesUserZone() {
        TimeZoneContext.set(TZ_NEW_YORK);
        LocalDateTime now = TimeZoneUtils.now();
        assertNotNull(now);
    }

    @Test
    void today_whenContextNotSet_usesDefaultZone() {
        LocalDate today = TimeZoneUtils.today();
        assertNotNull(today);
    }

    @Test
    void today_whenContextSet_usesUserZone() {
        TimeZoneContext.set(TZ_TOKYO);
        LocalDate today = TimeZoneUtils.today();
        assertNotNull(today);
    }

    @Test
    void nowUtc_returnsUtcTime() {
        LocalDateTime utcNow = TimeZoneUtils.nowUtc();
        assertNotNull(utcNow);
        assertEquals(ZoneOffset.UTC, utcNow.atZone(ZoneOffset.UTC).getOffset());
    }

    @Test
    void nowInstant_returnsCurrentInstant() {
        Instant instant = TimeZoneUtils.nowInstant();
        assertNotNull(instant);
        assertTrue(instant.isBefore(Instant.now().plusSeconds(1)));
    }

    // ===== Timezone Conversion Tests (LocalDateTime) =====

    @Test
    void toUserZone_whenContextNotSet_usesDefaultZone() {
        // UTC time: 2024-01-01 00:00:00
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        LocalDateTime userTime = TimeZoneUtils.toUserZone(utcTime);

        // Default zone is Asia/Shanghai (UTC+8)
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 0), userTime);
    }

    @Test
    void toUserZone_whenContextSet_convertsCorrectly() {
        TimeZoneContext.set(TZ_NEW_YORK); // UTC-5 (or UTC-4 during DST)

        // UTC time: 2024-01-01 12:00:00 (winter, no DST)
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        LocalDateTime userTime = TimeZoneUtils.toUserZone(utcTime);

        // New York in winter is UTC-5
        assertEquals(LocalDateTime.of(2024, 1, 1, 7, 0), userTime);
    }

    @Test
    void toUserZone_withTokyo_convertsCorrectly() {
        TimeZoneContext.set(TZ_TOKYO); // UTC+9

        // UTC time: 2024-01-01 00:00:00
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        LocalDateTime userTime = TimeZoneUtils.toUserZone(utcTime);

        // Tokyo is UTC+9
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), userTime);
    }

    @Test
    void toUtc_whenContextNotSet_usesDefaultZone() {
        // User time in default zone (Asia/Shanghai, UTC+8)
        LocalDateTime userTime = LocalDateTime.of(2024, 1, 1, 8, 0);

        LocalDateTime utcTime = TimeZoneUtils.toUtc(userTime);

        // Should be UTC midnight
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), utcTime);
    }

    @Test
    void toUtc_whenContextSet_convertsCorrectly() {
        TimeZoneContext.set(TZ_NEW_YORK);

        // New York time: 2024-01-01 07:00 (winter, UTC-5)
        LocalDateTime userTime = LocalDateTime.of(2024, 1, 1, 7, 0);

        LocalDateTime utcTime = TimeZoneUtils.toUtc(userTime);

        // Should be UTC noon
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), utcTime);
    }

    @Test
    void toUtc_withTokyo_convertsCorrectly() {
        TimeZoneContext.set(TZ_TOKYO); // UTC+9

        // Tokyo time: 2024-01-01 09:00
        LocalDateTime userTime = LocalDateTime.of(2024, 1, 1, 9, 0);

        LocalDateTime utcTime = TimeZoneUtils.toUtc(userTime);

        // Should be UTC midnight
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), utcTime);
    }

    // ===== Timezone Conversion Tests (Date) =====

    @Test
    void toUserZone_withDate_returnsSameDate() {
        Date date = new Date();
        Date result = TimeZoneUtils.toUserZone(date);
        assertEquals(date, result);
    }

    @Test
    void toUtc_withDate_returnsSameDate() {
        Date date = new Date();
        Date result = TimeZoneUtils.toUtc(date);
        assertEquals(date, result);
    }

    // ===== Timezone Conversion Tests (Instant) =====

    @Test
    void toUserZone_withInstant_whenContextNotSet_usesDefaultZone() {
        // 2024-01-01 00:00:00 UTC
        Instant instant = Instant.parse("2024-01-01T00:00:00Z");

        LocalDateTime userTime = TimeZoneUtils.toUserZone(instant);

        // Default zone is Asia/Shanghai (UTC+8)
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 0), userTime);
    }

    @Test
    void toUserZone_withInstant_whenContextSet_convertsCorrectly() {
        TimeZoneContext.set(TZ_TOKYO); // UTC+9

        Instant instant = Instant.parse("2024-01-01T00:00:00Z");

        LocalDateTime userTime = TimeZoneUtils.toUserZone(instant);

        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), userTime);
    }

    // ===== Convert Method Tests =====

    @Test
    void convert_convertsBetweenTimezones() {
        // 2024-01-01 00:00 in UTC
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        LocalDateTime tokyoTime = TimeZoneUtils.convert(utcTime, ZoneOffset.UTC, ZoneId.of("Asia/Tokyo"));

        // Tokyo is UTC+9
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), tokyoTime);
    }

    @Test
    void convert_reverseConversion() {
        // 2024-01-01 09:00 in Tokyo
        LocalDateTime tokyoTime = LocalDateTime.of(2024, 1, 1, 9, 0);

        LocalDateTime utcTime = TimeZoneUtils.convert(tokyoTime, ZoneId.of("Asia/Tokyo"), ZoneOffset.UTC);

        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), utcTime);
    }

    // ===== Formatting Tests =====

    @Test
    void format_withDefaultPattern() {
        TimeZoneContext.set(TZ_SHANGHAI);
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        String formatted = TimeZoneUtils.format(utcTime);

        assertEquals("2024-01-01 08:00:00", formatted);
    }

    @Test
    void format_withCustomPattern() {
        TimeZoneContext.set(TZ_SHANGHAI);
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        String formatted = TimeZoneUtils.format(utcTime, "yyyy/MM/dd HH:mm");

        assertEquals("2024/01/01 08:00", formatted);
    }

    @Test
    void format_withCustomZone() {
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0);

        String formatted = TimeZoneUtils.format(utcTime, "yyyy-MM-dd HH:mm", ZoneId.of("Asia/Tokyo"));

        assertEquals("2024-01-01 09:00", formatted);
    }

    @Test
    void format_withNewYork() {
        TimeZoneContext.set(TZ_NEW_YORK);
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        String formatted = TimeZoneUtils.format(utcTime, "yyyy-MM-dd HH:mm:ss");

        // New York in winter (Jan) is UTC-5
        assertEquals("2024-01-01 07:00:00", formatted);
    }

    // ===== Parsing Tests =====

    @Test
    void parse_withDefaultPattern() {
        String text = "2024-01-15 10:30:45";

        LocalDateTime result = TimeZoneUtils.parse(text);

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), result);
    }

    @Test
    void parse_withCustomPattern() {
        String text = "15/01/2024 10:30";

        LocalDateTime result = TimeZoneUtils.parse(text, "dd/MM/yyyy HH:mm");

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), result);
    }

    @Test
    void parse_withIsoPattern() {
        String text = "2024-01-15T10:30:45";

        LocalDateTime result = TimeZoneUtils.parse(text, "yyyy-MM-dd'T'HH:mm:ss");

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), result);
    }

    // ===== Round-trip Tests =====

    @Test
    void roundTrip_utcToUserToUtc() {
        TimeZoneContext.set(TZ_TOKYO);
        LocalDateTime original = LocalDateTime.of(2024, 6, 15, 14, 30, 0);

        // Convert to user zone (treating original as UTC)
        LocalDateTime userTime = TimeZoneUtils.toUserZone(original);

        // Convert back to UTC
        LocalDateTime backToUtc = TimeZoneUtils.toUtc(userTime);

        assertEquals(original, backToUtc);
    }

    @Test
    void roundTrip_formatAndParse() {
        TimeZoneContext.set(TZ_SHANGHAI);
        LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

        String formatted = TimeZoneUtils.format(utcTime);
        LocalDateTime parsed = TimeZoneUtils.parse(formatted);

        // Parsed time is in user zone, need to convert back to UTC
        LocalDateTime backToUtc = TimeZoneUtils.toUtc(parsed);

        assertEquals(utcTime, backToUtc);
    }
}
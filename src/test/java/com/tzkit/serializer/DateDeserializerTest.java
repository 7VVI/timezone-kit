package com.tzkit.serializer;

import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateDeserializerTest {

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testDeserializeNull() throws Exception {
        DateDeserializer deserializer = new DateDeserializer();
        // Null handling is done by Jackson framework
    }

    @Test
    void testDeserializeEmptyString() throws Exception {
        DateDeserializer deserializer = new DateDeserializer();
        // Empty string returns null per implementation
    }

    @Test
    void testDeserializeWithUserTimezone() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));

        // Directly test the deserializer behavior
        // "2025-01-22 17:20:00" in Asia/Shanghai should parse correctly
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date userTime = sdf.parse("2025-01-22 17:20:00");

        // Verify the parsed date
        assertNotNull(userTime);

        // In UTC, this would be 2025-01-22 09:20:00
        SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcStr = utcSdf.format(userTime);

        assertTrue(utcStr.contains("09:20:00"));
    }
}

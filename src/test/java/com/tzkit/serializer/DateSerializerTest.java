package com.tzkit.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateSerializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());
        mapper.registerModule(module);
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testSerializeNull() throws Exception {
        TestBean bean = new TestBean();
        bean.setDate(null);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"date\":null"));
    }

    @Test
    void testSerializeWithUserTimezoneShanghai() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));

        // 2025-01-22 09:20:00 UTC = 1737537600000 epoch millis
        Date utcDate = new Date(1737537600000L);
        TestBean bean = new TestBean();
        bean.setDate(utcDate);

        String json = mapper.writeValueAsString(bean);
        // Shanghai is UTC+8, so 09:20:00 UTC becomes 17:20:00 CST
        assertTrue(json.contains("2025-01-22 17:20:00"),
            "Expected date string in Asia/Shanghai timezone, got: " + json);
    }

    @Test
    void testSerializeWithUserTimezoneNewYork() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("America/New_York"));

        // 2025-01-22 09:20:00 UTC
        Date utcDate = new Date(1737537600000L);
        TestBean bean = new TestBean();
        bean.setDate(utcDate);

        String json = mapper.writeValueAsString(bean);
        // New York in January is EST (UTC-5), so 09:20:00 UTC becomes 04:20:00 EST
        assertTrue(json.contains("2025-01-22 04:20:00"),
            "Expected date string in America/New_York timezone, got: " + json);
    }

    @Test
    void testSerializeWithDefaultTimezone() throws Exception {
        // No timezone set in context, should default to Asia/Shanghai
        Date utcDate = new Date(1737537600000L);
        TestBean bean = new TestBean();
        bean.setDate(utcDate);

        String json = mapper.writeValueAsString(bean);
        // Default is Asia/Shanghai (UTC+8)
        assertTrue(json.contains("2025-01-22 17:20:00"),
            "Expected date string in default Asia/Shanghai timezone, got: " + json);
    }

    @Test
    void testSerializeUTC() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("UTC"));

        Date utcDate = new Date(1737537600000L);
        TestBean bean = new TestBean();
        bean.setDate(utcDate);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("2025-01-22 09:20:00"),
            "Expected date string in UTC, got: " + json);
    }

    @Test
    void testSerializeTokyo() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Tokyo"));

        // 2025-01-22 09:20:00 UTC
        Date utcDate = new Date(1737537600000L);
        TestBean bean = new TestBean();
        bean.setDate(utcDate);

        String json = mapper.writeValueAsString(bean);
        // Tokyo is UTC+9, so 09:20:00 UTC becomes 18:20:00 JST
        assertTrue(json.contains("2025-01-22 18:20:00"),
            "Expected date string in Asia/Tokyo timezone, got: " + json);
    }

    static class TestBean {
        private Date date;
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}

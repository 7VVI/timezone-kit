package com.tzkit.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateDeserializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializer());
        mapper.registerModule(module);
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testDeserializeNull() throws Exception {
        String json = "{\"date\":null}";
        TestBean bean = mapper.readValue(json, TestBean.class);
        assertNull(bean.getDate());
    }

    @Test
    void testDeserializeWithUserTimezoneShanghai() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));

        // "2025-01-22 17:20:00" in Asia/Shanghai (UTC+8) = 2025-01-22 09:20:00 UTC
        String json = "{\"date\":\"2025-01-22 17:20:00\"}";
        TestBean bean = mapper.readValue(json, TestBean.class);

        assertNotNull(bean.getDate());
        // Verify the resulting Date represents 2025-01-22 09:20:00 UTC
        assertEquals(1737537600000L, bean.getDate().getTime(),
            "Expected epoch millis for 2025-01-22 09:20:00 UTC");
    }

    @Test
    void testDeserializeWithUserTimezoneNewYork() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("America/New_York"));

        // "2025-01-22 04:20:00" in EST (UTC-5) = 2025-01-22 09:20:00 UTC
        String json = "{\"date\":\"2025-01-22 04:20:00\"}";
        TestBean bean = mapper.readValue(json, TestBean.class);

        assertNotNull(bean.getDate());
        assertEquals(1737537600000L, bean.getDate().getTime(),
            "Expected epoch millis for 2025-01-22 09:20:00 UTC");
    }

    @Test
    void testDeserializeWithDefaultTimezone() throws Exception {
        // No timezone set, should default to Asia/Shanghai
        String json = "{\"date\":\"2025-01-22 17:20:00\"}";
        TestBean bean = mapper.readValue(json, TestBean.class);

        assertNotNull(bean.getDate());
        // 17:20:00 Shanghai (UTC+8) = 09:20:00 UTC
        assertEquals(1737537600000L, bean.getDate().getTime(),
            "Expected epoch millis for 2025-01-22 09:20:00 UTC using default Asia/Shanghai");
    }

    @Test
    void testDeserializeUTC() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("UTC"));

        // "2025-01-22 09:20:00" in UTC
        String json = "{\"date\":\"2025-01-22 09:20:00\"}";
        TestBean bean = mapper.readValue(json, TestBean.class);

        assertNotNull(bean.getDate());
        assertEquals(1737537600000L, bean.getDate().getTime(),
            "Expected epoch millis for 2025-01-22 09:20:00 UTC");
    }

    @Test
    void testDeserializeTokyo() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Tokyo"));

        // "2025-01-22 18:20:00" in JST (UTC+9) = 2025-01-22 09:20:00 UTC
        String json = "{\"date\":\"2025-01-22 18:20:00\"}";
        TestBean bean = mapper.readValue(json, TestBean.class);

        assertNotNull(bean.getDate());
        assertEquals(1737537600000L, bean.getDate().getTime(),
            "Expected epoch millis for 2025-01-22 09:20:00 UTC");
    }

    @Test
    void testRoundTripSerialization() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));

        // Serialize a date, then deserialize it back - should get the same epoch millis
        Date original = new Date(1737537600000L);
        TestBean writeBean = new TestBean();
        writeBean.setDate(original);

        // Register both serializer and deserializer for round-trip
        ObjectMapper roundTripMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());
        module.addDeserializer(Date.class, new DateDeserializer());
        roundTripMapper.registerModule(module);

        String json = roundTripMapper.writeValueAsString(writeBean);
        TestBean readBean = roundTripMapper.readValue(json, TestBean.class);

        assertEquals(original.getTime(), readBean.getDate().getTime(),
            "Round-trip serialization should preserve the same instant");
    }

    static class TestBean {
        private Date date;
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}

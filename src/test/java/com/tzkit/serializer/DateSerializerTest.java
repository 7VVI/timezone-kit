package com.tzkit.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateSerializerTest {

    private ObjectMapper mapper;
    private TestBean bean;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testSerializeNull() throws Exception {
        bean = new TestBean();
        bean.setDate(null);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"date\":null"));
    }

    @Test
    void testSerializeWithUserTimezone() throws Exception {
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));

        // 2025-01-22 09:20:00 UTC = 1737537600000 epoch millis
        Date utcDate = new Date(1737537600000L);
        bean = new TestBean();
        bean.setDate(utcDate);

        // 09:20:00 UTC + 8 hours = 17:20:00 Asia/Shanghai
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String expected = sdf.format(utcDate);

        assertTrue(expected.contains("17:20:00"));
    }

    static class TestBean {
        private Date date;
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}

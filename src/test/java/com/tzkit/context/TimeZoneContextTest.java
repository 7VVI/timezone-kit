package com.tzkit.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneContextTest {

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testSetAndGet() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);

        assertEquals(tz, TimeZoneContext.get());
    }

    @Test
    void testGetWhenNotSet() {
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testClear() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);
        TimeZoneContext.clear();

        assertNull(TimeZoneContext.get());
    }

    @Test
    void testGetZoneId() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);

        assertEquals(tz.toZoneId(), TimeZoneContext.getZoneId());
    }

    @Test
    void testGetZoneIdWhenNotSet() {
        assertNull(TimeZoneContext.getZoneId());
    }

    @Test
    void testThreadIsolation() throws Exception {
        TimeZone tz1 = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone tz2 = TimeZone.getTimeZone("America/New_York");

        TimeZoneContext.set(tz1);

        Thread otherThread = new Thread(() -> {
            assertNull(TimeZoneContext.get());
            TimeZoneContext.set(tz2);
            assertEquals(tz2, TimeZoneContext.get());
        });

        otherThread.start();
        otherThread.join();

        // Main thread still has tz1
        assertEquals(tz1, TimeZoneContext.get());
    }
}

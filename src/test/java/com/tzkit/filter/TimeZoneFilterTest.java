package com.tzkit.filter;

import com.tzkit.config.TimeZoneProperties;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneFilterTest {

    private TimeZoneFilter filter;
    private TimeZoneProperties properties;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        properties = new TimeZoneProperties();
        filter = new TimeZoneFilter(properties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    /**
     * Get the timezone captured during filter chain execution.
     */
    private TimeZone getCapturedTimezone() {
        return filterChain.capturedTimezone;
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testExtractIANATimezone() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testExtractUtcOffsetPositive() throws Exception {
        request.addHeader("Time-Zone-Offset", "+8");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("GMT+08:00", tz.getID());
    }

    @Test
    void testExtractUtcOffsetNegative() throws Exception {
        request.addHeader("Time-Zone-Offset", "-5");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("GMT-05:00", tz.getID());
    }

    @Test
    void testExtractUtcOffsetWithMinutes() throws Exception {
        request.addHeader("Time-Zone-Offset", "+5:30");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("GMT+05:30", tz.getID());
    }

    @Test
    void testPreferIANAOverOffset() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        request.addHeader("Time-Zone-Offset", "+8");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testUseDefaultWhenNoHeader() throws Exception {
        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testInvalidTimezoneFallsBackToDefault() throws Exception {
        request.addHeader("Time-Zone", "Invalid/Zone");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testCustomHeaderNames() throws Exception {
        properties.getHeader().setTimezone("X-Timezone");
        properties.getHeader().setOffset("X-Offset");
        filter = new TimeZoneFilter(properties);

        request.addHeader("X-Timezone", "America/New_York");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertEquals("America/New_York", tz.getID());
    }

    @Test
    void testCustomDefaultTimezone() throws Exception {
        properties.setDefaultTimezone("America/New_York");
        filter = new TimeZoneFilter(properties);

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertEquals("America/New_York", tz.getID());
    }

    @Test
    void testCleanupAfterFilter() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");

        filter.doFilter(request, response, filterChain);

        // Context should be cleared after filter chain completes
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testCleanupAfterException() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        MockFilterChain throwingChain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res)
                throws IOException, ServletException {
                throw new ServletException("Test exception");
            }
        };

        assertThrows(ServletException.class, () ->
            filter.doFilter(request, response, throwingChain));

        // Context should still be cleared after exception
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testCleanupAfterErrorResponse() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        MockFilterChain errorChain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res)
                throws IOException, ServletException {
                ((MockHttpServletResponse) res).setStatus(500);
            }
        };

        filter.doFilter(request, response, errorChain);

        // Context should be cleared even after error response
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testExplicitGMTTimezone() throws Exception {
        request.addHeader("Time-Zone", "GMT");

        filter.doFilter(request, response, filterChain);

        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("GMT", tz.getID());
    }

    @Test
    void testMalformedOffsetFormat() throws Exception {
        request.addHeader("Time-Zone-Offset", "abc");

        filter.doFilter(request, response, filterChain);

        // Malformed offset "abc" should fall back to default timezone
        TimeZone tz = getCapturedTimezone();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    private static class MockFilterChain implements FilterChain {
        TimeZone capturedTimezone;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
            // Capture the timezone during filter chain execution
            capturedTimezone = TimeZoneContext.get();
        }
    }
}
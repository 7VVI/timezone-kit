package com.tzkit.filter;

import com.tzkit.config.TimeZoneProperties;
import com.tzkit.context.TimeZoneContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Servlet filter that extracts timezone from request headers
 * and stores it in TimeZoneContext for the request lifecycle.
 */
public class TimeZoneFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneFilter.class);

    private final TimeZoneProperties properties;

    public TimeZoneFilter(TimeZoneProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            TimeZone timeZone = resolveTimeZone(httpRequest);
            TimeZoneContext.set(timeZone);

            chain.doFilter(request, response);
        } finally {
            // Always clear to prevent memory leaks
            TimeZoneContext.clear();
        }
    }

    /**
     * Resolve timezone from request headers.
     * Priority: Time-Zone header -> Time-Zone-Offset header -> default
     */
    private TimeZone resolveTimeZone(HttpServletRequest request) {
        String timezoneHeader = request.getHeader(properties.getHeader().getTimezone());
        String offsetHeader = request.getHeader(properties.getHeader().getOffset());

        // Priority 1: IANA timezone ID
        if (timezoneHeader != null && !timezoneHeader.isEmpty()) {
            TimeZone tz = parseIANATimezone(timezoneHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid timezone header: {}, using default", timezoneHeader);
        }

        // Priority 2: UTC offset
        if (offsetHeader != null && !offsetHeader.isEmpty()) {
            TimeZone tz = parseOffsetTimezone(offsetHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid offset header: {}, using default", offsetHeader);
        }

        // Priority 3: Default timezone
        return TimeZone.getTimeZone(properties.getDefaultTimezone());
    }

    /**
     * Parse IANA timezone ID (e.g., "Asia/Shanghai")
     */
    private TimeZone parseIANATimezone(String timezoneId) {
        try {
            TimeZone tz = TimeZone.getTimeZone(timezoneId);
            // TimeZone.getTimeZone returns GMT for invalid IDs
            if (!tz.getID().equals("GMT") || timezoneId.equals("GMT")) {
                return tz;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse UTC offset (e.g., "+8", "-5", "+5:30")
     */
    private TimeZone parseOffsetTimezone(String offset) {
        try {
            String normalized = normalizeOffset(offset);
            if (normalized != null) {
                return TimeZone.getTimeZone("GMT" + normalized);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Normalize offset format to +/-HH:mm or +/-HH
     * Input: "+8", "-5", "+5:30", "+05:30"
     * Output: "+08:00", "-05:00", "+05:30", "+05:30"
     */
    private String normalizeOffset(String offset) {
        if (offset == null || offset.isEmpty()) {
            return null;
        }

        // Remove leading + if present, keep -
        String sign = offset.startsWith("-") ? "-" : "+";
        String value = offset.startsWith("+") || offset.startsWith("-")
            ? offset.substring(1) : offset;

        // Split by : if present
        String[] parts = value.split(":");

        String hours;
        String minutes = "00";

        if (parts.length == 1) {
            hours = padZero(parts[0], 2);
        } else if (parts.length == 2) {
            hours = padZero(parts[0], 2);
            minutes = padZero(parts[1], 2);
        } else {
            return null;
        }

        return sign + hours + ":" + minutes;
    }

    private String padZero(String s, int length) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
}
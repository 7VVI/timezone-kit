package com.tzkit.context;

import java.util.TimeZone;
import java.time.ZoneId;

/**
 * ThreadLocal holder for current request's user timezone.
 * Automatically cleared by TimeZoneFilter after request completion.
 */
public final class TimeZoneContext {

    private static final ThreadLocal<TimeZone> TIME_ZONE_HOLDER = new ThreadLocal<>();

    private TimeZoneContext() {
        // Utility class
    }

    /**
     * Get current request's user timezone.
     * @return TimeZone or null if not set
     */
    public static TimeZone get() {
        return TIME_ZONE_HOLDER.get();
    }

    /**
     * Set current request's user timezone.
     * @param timeZone the timezone to store
     */
    public static void set(TimeZone timeZone) {
        TIME_ZONE_HOLDER.set(timeZone);
    }

    /**
     * Clear the timezone context to prevent memory leaks.
     */
    public static void clear() {
        TIME_ZONE_HOLDER.remove();
    }

    /**
     * Get current request's user ZoneId.
     * @return ZoneId or null if not set
     */
    public static ZoneId getZoneId() {
        TimeZone tz = get();
        return tz != null ? tz.toZoneId() : null;
    }
}

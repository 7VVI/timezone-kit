package com.tzkit.context;

import java.util.TimeZone;
import java.time.ZoneId;

/**
 * 时区上下文持有者
 * 用于存储当前请求的用户时区
 * 请求结束后由 TimeZoneFilter 自动清理
 */
public final class TimeZoneContext {

    private static final ThreadLocal<TimeZone> TIME_ZONE_HOLDER = new ThreadLocal<>();

    private TimeZoneContext() {
        // 工具类，禁止实例化
    }

    /**
     * 获取当前请求的用户时区
     * @return 时区对象，未设置时返回 null
     */
    public static TimeZone get() {
        return TIME_ZONE_HOLDER.get();
    }

    /**
     * 设置当前请求的用户时区
     * @param timeZone 要存储的时区
     */
    public static void set(TimeZone timeZone) {
        TIME_ZONE_HOLDER.set(timeZone);
    }

    /**
     * 清理时区上下文，防止内存泄漏
     */
    public static void clear() {
        TIME_ZONE_HOLDER.remove();
    }

    /**
     * 获取当前请求的用户 ZoneId
     * @return ZoneId 对象，未设置时返回 null
     */
    public static ZoneId getZoneId() {
        TimeZone tz = get();
        return tz != null ? tz.toZoneId() : null;
    }
}

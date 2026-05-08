package com.tzkit.context;

import java.util.TimeZone;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * 服务器时区配置持有者
 * 存储后端服务的基准时区配置
 */
public final class TimeZoneContextHolder {

    private static volatile TimeZone serverTimeZone = TimeZone.getTimeZone("UTC");
    private static volatile ZoneId serverZoneId = ZoneId.of("UTC");

    private TimeZoneContextHolder() {}

    /**
     * 设置服务器时区
     * @param timeZone 服务器时区
     */
    public static void setServerTimeZone(TimeZone timeZone) {
        serverTimeZone = timeZone;
        serverZoneId = timeZone.toZoneId();
    }

    /**
     * 设置服务器ZoneId
     * @param zoneId 服务器ZoneId
     */
    public static void setServerZoneId(ZoneId zoneId) {
        serverZoneId = zoneId;
        serverTimeZone = TimeZone.getTimeZone(zoneId);
    }

    /**
     * 获取服务器时区
     * @return 服务器时区
     */
    public static TimeZone getServerTimeZone() {
        return serverTimeZone;
    }

    /**
     * 获取服务器ZoneId
     * @return 服务器ZoneId
     */
    public static ZoneId getServerZoneId() {
        return serverZoneId;
    }

    /**
     * 获取服务器ZoneOffset
     * @return 服务器ZoneOffset
     */
    public static ZoneOffset getServerZoneOffset() {
        return serverZoneId.getRules().getOffset(java.time.Instant.now());
    }
}
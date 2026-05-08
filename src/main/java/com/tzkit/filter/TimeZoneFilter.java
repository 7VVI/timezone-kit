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
 * Servlet 过滤器，从请求头中提取时区信息
 * 并将其存储在 TimeZoneContext 中，供请求生命周期使用
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
            // 始终清理，防止内存泄漏
            TimeZoneContext.clear();
        }
    }

    /**
     * 从请求头中解析时区
     * 优先级: Time-Zone 请求头 -> Time-Zone-Offset 请求头 -> 默认时区
     */
    private TimeZone resolveTimeZone(HttpServletRequest request) {
        String timezoneHeader = request.getHeader(properties.getHeader().getTimezone());
        String offsetHeader = request.getHeader(properties.getHeader().getOffset());

        // 优先级1: IANA 时区 ID
        if (timezoneHeader != null && !timezoneHeader.isEmpty()) {
            TimeZone tz = parseIANATimezone(timezoneHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid timezone header: {}, using default", timezoneHeader);
        }

        // 优先级2: UTC 偏移量
        if (offsetHeader != null && !offsetHeader.isEmpty()) {
            TimeZone tz = parseOffsetTimezone(offsetHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid offset header: {}, using default", offsetHeader);
        }

        // 优先级3: 默认时区
        return TimeZone.getTimeZone(properties.getDefaultTimezone());
    }

    /**
     * 解析 IANA 时区 ID（例如: "Asia/Shanghai"）
     */
    private TimeZone parseIANATimezone(String timezoneId) {
        try {
            TimeZone tz = TimeZone.getTimeZone(timezoneId);
            // TimeZone.getTimeZone 对于无效的 ID 会返回 GMT
            if (!tz.getID().equals("GMT") || timezoneId.equals("GMT")) {
                return tz;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 UTC 偏移量（例如: "+8", "-5", "+5:30"）
     */
    private TimeZone parseOffsetTimezone(String offset) {
        try {
            String normalized = normalizeOffset(offset);
            if (normalized != null) {
                TimeZone tz = TimeZone.getTimeZone("GMT" + normalized);
                // TimeZone.getTimeZone 对于无效偏移量（如 "+99:99"）会返回 GMT
                if (!tz.getID().equals("GMT") || ("GMT" + normalized).equals("GMT")) {
                    return tz;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 规范化偏移量格式为 +/-HH:mm 或 +/-HH
     * 输入: "+8", "-5", "+5:30", "+05:30"
     * 输出: "+08:00", "-05:00", "+05:30", "+05:30"
     */
    private String normalizeOffset(String offset) {
        if (offset == null || offset.isEmpty()) {
            return null;
        }

        // 移除前导 +，保留 -
        String sign = offset.startsWith("-") ? "-" : "+";
        String value = offset.startsWith("+") || offset.startsWith("-")
            ? offset.substring(1) : offset;

        // 校验: 只能包含数字和可选的冒号
        if (!value.matches("[0-9:]+")) {
            return null; // 无效格式，回退到默认时区
        }

        // 按冒号分割（如果存在）
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
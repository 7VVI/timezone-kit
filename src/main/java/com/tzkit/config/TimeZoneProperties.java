package com.tzkit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TZKit 配置属性
 * 配置前缀: tzkit
 */
@ConfigurationProperties(prefix = "tzkit")
public class TimeZoneProperties {

    /**
     * 请求头配置
     */
    private Header header = new Header();

    /**
     * 默认时区（未提供请求头时使用）
     */
    private String defaultTimezone = "Asia/Shanghai";

    /**
     * 后端服务时区（数据存储/处理的基准时区）
     * 默认为 UTC
     */
    private String serverTimezone = "UTC";

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public void setDefaultTimezone(String defaultTimezone) {
        this.defaultTimezone = defaultTimezone;
    }

    public String getServerTimezone() {
        return serverTimezone;
    }

    public void setServerTimezone(String serverTimezone) {
        this.serverTimezone = serverTimezone;
    }

    /**
     * 请求头名称配置
     */
    public static class Header {
        /**
         * 时区 ID (IANA) 请求头名称
         */
        private String timezone = "Time-Zone";

        /**
         * UTC 偏移量请求头名称
         */
        private String offset = "Time-Zone-Offset";

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getOffset() {
            return offset;
        }

        public void setOffset(String offset) {
            this.offset = offset;
        }
    }
}

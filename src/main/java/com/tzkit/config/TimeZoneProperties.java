package com.tzkit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for TZKit.
 * Prefix: tzkit
 */
@ConfigurationProperties(prefix = "tzkit")
public class TimeZoneProperties {

    /**
     * Header configuration
     */
    private Header header = new Header();

    /**
     * Default timezone when no header provided
     */
    private String defaultTimezone = "Asia/Shanghai";

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

    /**
     * Header name configuration
     */
    public static class Header {
        /**
         * Request header name for timezone ID (IANA)
         */
        private String timezone = "Time-Zone";

        /**
         * Request header name for UTC offset
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

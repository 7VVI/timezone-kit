package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Custom deserializer for java.time.LocalDateTime.
 * Parses date string in user timezone and converts to UTC LocalDateTime.
 * Supports @JsonFormat annotation for per-field pattern and timezone override.
 * Supports multiple date/time formats for flexible parsing.
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
    implements ContextualDeserializer {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DEFAULT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    // Supported formats for flexible parsing
    private static final DateTimeFormatter[] SUPPORTED_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE
    };

    private final String pattern;
    private final ZoneId timezone;

    public LocalDateTimeDeserializer() {
        this(null, null);
    }

    public LocalDateTimeDeserializer(String pattern, ZoneId timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        ZoneId zone = this.timezone;
        if (zone == null) {
            zone = TimeZoneContext.getZoneId();
            if (zone == null) {
                zone = DEFAULT_ZONE;
            }
        }

        LocalDateTime userTime;

        // If a pattern is specified (e.g. from @JsonFormat), use it
        if (this.pattern != null && !this.pattern.isEmpty()) {
            DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(this.pattern, Locale.ENGLISH);
            userTime = parseWithFormatter(text, formatter);
        } else {
            // Try multiple formats
            userTime = parseWithMultipleFormats(text);
        }

        // Convert user timezone LocalDateTime to UTC
        return userTime.atZone(zone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    private LocalDateTime parseWithFormatter(String text,
                                             DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(text, formatter);
        } catch (Exception e) {
            // Try parsing as LocalDate then convert to LocalDateTime
            try {
                LocalDate date = LocalDate.parse(text, formatter);
                return date.atStartOfDay();
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                    "Failed to parse date: " + text, ex);
            }
        }
    }

    private LocalDateTime parseWithMultipleFormats(String text) {
        for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (Exception ignored) {
                // Continue trying next format
            }
        }
        // Last resort: try ISO_LOCAL_DATE as fallback for pure date strings
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay();
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(
            "No format fit for date String: " + text);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String pattern = format.pattern();
            ZoneId zone = format.timezone() != null && !format.timezone().isEmpty()
                ? ZoneId.of(format.timezone())
                : null;
            return new LocalDateTimeDeserializer(
                pattern.isEmpty() ? null : pattern, zone);
        }

        return this;
    }
}

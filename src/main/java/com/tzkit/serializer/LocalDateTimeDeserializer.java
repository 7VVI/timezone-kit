package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tzkit.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Custom deserializer for java.time.LocalDateTime.
 * Parses date string in user timezone and converts to UTC LocalDateTime.
 * Supports @JsonFormat annotation for per-field pattern and timezone override.
 * Uses DateUtils for flexible multi-format parsing.
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
    implements ContextualDeserializer {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

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
            zone = DateUtils.getZoneId();
        }

        LocalDateTime userTime;

        // If a pattern is specified (e.g. from @JsonFormat), use it
        if (this.pattern != null && !this.pattern.isEmpty()) {
            userTime = DateUtils.parse(text, this.pattern);
        } else {
            // Use DateUtils multi-format parsing
            userTime = DateUtils.parseLdt(text);
        }

        // Convert user timezone LocalDateTime to UTC
        return userTime.atZone(zone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
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

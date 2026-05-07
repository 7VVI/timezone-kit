package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Custom serializer for java.time.LocalDateTime.
 * Converts UTC LocalDateTime to user timezone string representation.
 * Supports @JsonFormat annotation for per-field pattern and timezone override.
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>
    implements ContextualSerializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private final String pattern;
    private final ZoneId timezone;

    public LocalDateTimeSerializer() {
        this(DEFAULT_PATTERN, null);
    }

    public LocalDateTimeSerializer(String pattern, ZoneId timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        ZoneId zone = this.timezone;
        if (zone == null) {
            zone = TimeZoneContext.getZoneId();
            if (zone == null) {
                zone = DEFAULT_ZONE;
            }
        }

        // Convert UTC LocalDateTime to user timezone
        LocalDateTime userTime = value.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(zone)
            .toLocalDateTime();

        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(this.pattern, Locale.ENGLISH);
        gen.writeString(formatter.format(userTime));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov,
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
            return new LocalDateTimeSerializer(
                pattern.isEmpty() ? DEFAULT_PATTERN : pattern, zone);
        }

        return this;
    }
}

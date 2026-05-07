package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom serializer for java.util.Date.
 * Supports @JsonFormat annotation for per-field pattern and timezone overrides.
 * Falls back to TimeZoneContext for timezone resolution.
 */
public class DateSerializer extends JsonSerializer<Date> implements ContextualSerializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Asia/Shanghai");

    private final String pattern;
    private final TimeZone timezone;

    public DateSerializer() {
        this(DEFAULT_PATTERN, null);
    }

    public DateSerializer(String pattern, TimeZone timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        TimeZone tz = this.timezone;
        if (tz == null) {
            tz = TimeZoneContext.get();
            if (tz == null) {
                tz = DEFAULT_TIMEZONE;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(this.pattern, Locale.ENGLISH);
        sdf.setTimeZone(tz);
        gen.writeString(sdf.format(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String p = format.pattern();
            if (p.isEmpty()) {
                p = DEFAULT_PATTERN;
            }
            TimeZone tz = null;
            if (format.timezone() != null && !format.timezone().isEmpty()) {
                tz = TimeZone.getTimeZone(format.timezone());
            }
            return new DateSerializer(p, tz);
        }

        return this;
    }
}

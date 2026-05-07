package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tzkit.utils.DateUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom deserializer for java.util.Date.
 * Supports @JsonFormat annotation for per-field pattern and timezone overrides.
 * Falls back to DateUtils for timezone resolution and multi-format auto-parsing.
 */
public class DateDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final String pattern;
    private final TimeZone timezone;

    public DateDeserializer() {
        this(null, null);
    }

    public DateDeserializer(String pattern, TimeZone timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        TimeZone tz = this.timezone;
        if (tz == null) {
            tz = DateUtils.getTimeZone();
        }

        // If @JsonFormat specifies a pattern, use it for parsing
        if (this.pattern != null && !this.pattern.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(this.pattern, Locale.ENGLISH);
                sdf.setTimeZone(tz);
                return sdf.parse(text);
            } catch (ParseException e) {
                throw new IOException("Failed to parse date with pattern [" + this.pattern + "]: " + text, e);
            }
        }

        // Use DateUtils for multi-format auto-parsing
        try {
            return DateUtils.parse(text, tz);
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + text, e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String p = format.pattern();
            TimeZone tz = null;
            if (format.timezone() != null && !format.timezone().isEmpty()) {
                tz = TimeZone.getTimeZone(format.timezone());
            }
            return new DateDeserializer(p.isEmpty() ? null : p, tz);
        }

        return this;
    }
}

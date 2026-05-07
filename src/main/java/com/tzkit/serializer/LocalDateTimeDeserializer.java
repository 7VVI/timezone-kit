package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Custom deserializer for java.time.LocalDateTime.
 * Parses date string in user timezone and converts to UTC LocalDateTime.
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN, Locale.ENGLISH);
    static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        ZoneId userZone = TimeZoneContext.getZoneId();
        if (userZone == null) {
            userZone = DEFAULT_TIMEZONE;
        }

        try {
            LocalDateTime parsedTime = LocalDateTime.parse(text, FORMATTER);
            return parsedTime.atZone(userZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new IOException("Failed to parse LocalDateTime: " + text, e);
        }
    }
}

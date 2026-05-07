package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
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
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN, Locale.ENGLISH);
    static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        ZoneId userZone = TimeZoneContext.getZoneId();
        if (userZone == null) {
            userZone = DEFAULT_TIMEZONE;
        }

        LocalDateTime userTime = value.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(userZone)
            .toLocalDateTime();

        gen.writeString(userTime.format(FORMATTER));
    }
}

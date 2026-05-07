package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for java.time.Instant.
 * Outputs ISO-8601 UTC format with no timezone conversion
 * (Instant always represents a precise moment in UTC).
 */
public class InstantSerializer extends JsonSerializer<Instant> {

    @Override
    public void serialize(Instant value, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeString(DateTimeFormatter.ISO_INSTANT.format(value));
    }
}

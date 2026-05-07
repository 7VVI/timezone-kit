package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Custom deserializer for java.time.LocalDate.
 * Parses ISO date string. No timezone conversion (LocalDate is timezone-independent).
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }
        return LocalDate.parse(text);
    }
}

package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom deserializer for java.time.Instant.
 * Supports two input formats:
 * 1. ISO-8601 string (e.g. "2026-01-22T10:00:00Z")
 * 2. Numeric timestamp (epoch milliseconds)
 * No timezone conversion is performed (Instant is always UTC).
 */
public class InstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        if (p.hasToken(JsonToken.VALUE_STRING)) {
            String text = p.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            return Instant.parse(text);
        }

        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return Instant.ofEpochMilli(p.getLongValue());
        }

        return null;
    }
}

package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tzkit.context.TimeZoneContext;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom deserializer for java.util.Date.
 * Parses date string in user timezone and converts to UTC Date.
 */
public class DateDeserializer extends JsonDeserializer<Date> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Asia/Shanghai");

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        TimeZone userTz = TimeZoneContext.get();
        if (userTz == null) {
            userTz = DEFAULT_TIMEZONE;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN, Locale.ENGLISH);
            sdf.setTimeZone(userTz);
            Date parsedDate = sdf.parse(text);
            return parsedDate;
        } catch (ParseException e) {
            throw new IOException("Failed to parse date: " + text, e);
        }
    }
}

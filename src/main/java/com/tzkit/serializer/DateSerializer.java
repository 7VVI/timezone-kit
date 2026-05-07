package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom serializer for java.util.Date.
 * Converts UTC Date to user timezone string representation.
 */
public class DateSerializer extends JsonSerializer<Date> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void serialize(Date value, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        TimeZone userTz = TimeZoneContext.get();
        if (userTz == null) {
            userTz = TimeZone.getTimeZone("Asia/Shanghai");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN);
        sdf.setTimeZone(userTz);

        gen.writeString(sdf.format(value));
    }
}

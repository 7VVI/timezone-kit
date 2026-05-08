package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * java.time.Instant 自定义序列化器
 * 输出 ISO-8601 UTC 格式，不进行时区转换
 * （Instant 始终表示 UTC 中的精确时刻）
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

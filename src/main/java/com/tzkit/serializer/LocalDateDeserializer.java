package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;

/**
 * java.time.LocalDate 自定义反序列化器
 * 解析 ISO 日期字符串，不进行时区转换（LocalDate 与时区无关）
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

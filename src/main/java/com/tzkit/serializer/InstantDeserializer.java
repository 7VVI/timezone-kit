package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * java.time.Instant 自定义反序列化器
 * 支持两种输入格式:
 * 1. ISO-8601 字符串（例如: "2026-01-22T10:00:00Z"）
 * 2. 数字时间戳（epoch 毫秒）
 * 不进行时区转换（Instant 始终为 UTC）
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

package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tzkit.context.TimeZoneContextHolder;
import com.tzkit.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * java.time.LocalDateTime 自定义反序列化器
 * 在用户时区下解析日期字符串，并转换为服务器时区 LocalDateTime
 * 支持 @JsonFormat 注解的 pattern 和 timezone 覆盖
 * 使用 DateUtils 进行灵活的多格式解析
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
    implements ContextualDeserializer {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private final String pattern;
    private final ZoneId timezone;

    public LocalDateTimeDeserializer() {
        this(null, null);
    }

    public LocalDateTimeDeserializer(String pattern, ZoneId timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        ZoneId zone = this.timezone;
        if (zone == null) {
            zone = DateUtils.getZoneId();
        }

        LocalDateTime userTime;

        // 指定 pattern 时（例如来自 @JsonFormat），使用指定格式
        if (this.pattern != null && !this.pattern.isEmpty()) {
            userTime = DateUtils.parse(text, this.pattern);
        } else {
            // 使用 DateUtils 多格式解析
            userTime = DateUtils.parseLdt(text);
        }

        // 将用户时区 LocalDateTime 转换为服务器时区
        return userTime.atZone(zone)
            .withZoneSameInstant(TimeZoneContextHolder.getServerZoneId())
            .toLocalDateTime();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String pattern = format.pattern();
            ZoneId zone = format.timezone() != null && !format.timezone().isEmpty()
                ? ZoneId.of(format.timezone())
                : null;
            return new LocalDateTimeDeserializer(
                pattern.isEmpty() ? null : pattern, zone);
        }

        return this;
    }
}

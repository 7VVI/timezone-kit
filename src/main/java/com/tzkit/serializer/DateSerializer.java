package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.tzkit.utils.DateUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * java.util.Date 自定义序列化器
 * 支持 @JsonFormat 注解的 pattern 和 timezone 覆盖
 * 未指定 timezone 时通过 DateUtils 从 TimeZoneContext 获取用户时区
 */
public class DateSerializer extends JsonSerializer<Date> implements ContextualSerializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final String pattern;
    private final TimeZone timezone;

    public DateSerializer() {
        this(DEFAULT_PATTERN, null);
    }

    public DateSerializer(String pattern, TimeZone timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        String result;
        // @JsonFormat 指定了自定义时区
        if (this.timezone != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(this.pattern, java.util.Locale.ENGLISH);
            sdf.setTimeZone(this.timezone);
            result = sdf.format(value);
        } else {
            // 使用 DateUtils 和上下文中的用户时区
            result = DateUtils.format(value, this.pattern);
        }

        gen.writeString(result);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String p = format.pattern();
            if (p.isEmpty()) {
                p = DEFAULT_PATTERN;
            }
            TimeZone tz = null;
            if (format.timezone() != null && !format.timezone().isEmpty()) {
                tz = TimeZone.getTimeZone(format.timezone());
            }
            return new DateSerializer(p, tz);
        }

        return this;
    }
}

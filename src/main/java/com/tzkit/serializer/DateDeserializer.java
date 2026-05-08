package com.tzkit.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.tzkit.utils.DateUtils;

import cn.hutool.core.date.format.FastDateFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * java.util.Date 自定义反序列化器
 * 支持 @JsonFormat 注解的 pattern 和 timezone 覆盖
 * 未指定 pattern 时使用 DateUtils 进行多格式自动解析
 */
public class DateDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final String pattern;
    private final TimeZone timezone;

    public DateDeserializer() {
        this(null, null);
    }

    public DateDeserializer(String pattern, TimeZone timezone) {
        this.pattern = pattern;
        this.timezone = timezone;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {

        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        TimeZone tz = this.timezone;
        if (tz == null) {
            tz = DateUtils.getTimeZone();
        }

        // 指定 pattern 时使用指定格式
        if (this.pattern != null && !this.pattern.isEmpty()) {
            try {
                FastDateFormat sdf = FastDateFormat.getInstance(this.pattern, tz, Locale.ENGLISH);
                return sdf.parse(text);
            } catch (ParseException e) {
                throw new IOException("无法解析日期字符串: " + text + "，格式: " + this.pattern, e);
            }
        }

        // 使用 DateUtils 多格式自动解析
        try {
            return DateUtils.parse(text, tz);
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + text, e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }

        JsonFormat format = property.getAnnotation(JsonFormat.class);
        if (format != null) {
            String p = format.pattern();
            TimeZone tz = null;
            if (format.timezone() != null && !format.timezone().isEmpty()) {
                tz = TimeZone.getTimeZone(format.timezone());
            }
            return new DateDeserializer(p.isEmpty() ? null : p, tz);
        }

        return this;
    }
}

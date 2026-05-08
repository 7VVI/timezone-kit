package com.tzkit.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tzkit.serializer.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Jackson 时区序列化配置
 * 注册自定义序列化器/反序列化器，设置 UTC 为默认时区
 */
@Configuration
public class JacksonTimeZoneConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer tzkitJacksonCustomizer() {
        return builder -> {
            // 设置默认时区为 UTC
            builder.timeZone(TimeZone.getTimeZone("UTC"));

            // 禁用将日期写为时间戳格式
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // 创建自定义模块
            SimpleModule tzkitModule = new SimpleModule("TZKitModule");

            // 注册自定义序列化器
            tzkitModule.addSerializer(java.util.Date.class, new DateSerializer());
            tzkitModule.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer());
            tzkitModule.addSerializer(java.time.Instant.class, new InstantSerializer());
            tzkitModule.addSerializer(java.time.LocalDate.class, new LocalDateSerializer());

            // 注册自定义反序列化器
            tzkitModule.addDeserializer(java.util.Date.class, new DateDeserializer());
            tzkitModule.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer());
            tzkitModule.addDeserializer(java.time.Instant.class, new InstantDeserializer());
            tzkitModule.addDeserializer(java.time.LocalDate.class, new LocalDateDeserializer());

            builder.modules(tzkitModule, new JavaTimeModule());
        };
    }
}

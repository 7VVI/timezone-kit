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
 * Jackson configuration for timezone-aware serialization.
 * Registers custom serializers/deserializers and sets UTC as default timezone.
 */
@Configuration
public class JacksonTimeZoneConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer tzkitJacksonCustomizer() {
        return builder -> {
            // Set default timezone to UTC
            builder.timeZone(TimeZone.getTimeZone("UTC"));

            // Disable writing dates as timestamps
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Create custom module for our serializers
            SimpleModule tzkitModule = new SimpleModule("TZKitModule");

            // Register custom serializers
            tzkitModule.addSerializer(java.util.Date.class, new DateSerializer());
            tzkitModule.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer());
            tzkitModule.addSerializer(java.time.Instant.class, new InstantSerializer());
            tzkitModule.addSerializer(java.time.LocalDate.class, new LocalDateSerializer());

            // Register custom deserializers
            tzkitModule.addDeserializer(java.util.Date.class, new DateDeserializer());
            tzkitModule.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer());
            tzkitModule.addDeserializer(java.time.Instant.class, new InstantDeserializer());
            tzkitModule.addDeserializer(java.time.LocalDate.class, new LocalDateDeserializer());

            builder.modules(tzkitModule, new JavaTimeModule());
        };
    }
}

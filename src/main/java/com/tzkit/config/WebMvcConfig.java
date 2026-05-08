package com.tzkit.config;

import com.tzkit.converter.UserTimeZoneDateConverter;
import com.tzkit.converter.UserTimeZoneLocalDateConverter;
import com.tzkit.converter.UserTimeZoneLocalDateTimeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册时间参数转换器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new UserTimeZoneDateConverter());
        registry.addConverter(new UserTimeZoneLocalDateConverter());
        registry.addConverter(new UserTimeZoneLocalDateTimeConverter());
    }
}

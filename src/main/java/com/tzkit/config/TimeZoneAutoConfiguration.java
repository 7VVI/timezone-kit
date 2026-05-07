package com.tzkit.config;

import com.tzkit.filter.TimeZoneFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;

/**
 * Auto-configuration for TZKit Spring Boot Starter.
 * Activates on Servlet-based web applications.
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(TimeZoneProperties.class)
@Import(JacksonTimeZoneConfig.class)
public class TimeZoneAutoConfiguration {

    /**
     * Register TimeZoneFilter with highest priority.
     */
    @Bean
    public FilterRegistrationBean<TimeZoneFilter> timeZoneFilterRegistration(
            TimeZoneProperties properties) {

        TimeZoneFilter filter = new TimeZoneFilter(properties);

        FilterRegistrationBean<TimeZoneFilter> registration =
            new FilterRegistrationBean<>(filter);

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(
            DispatcherType.REQUEST,
            DispatcherType.ASYNC,
            DispatcherType.ERROR
        );

        return registration;
    }
}

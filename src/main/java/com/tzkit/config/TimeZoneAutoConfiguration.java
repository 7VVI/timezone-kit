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
 * TZKit Spring Boot Starter 自动配置
 * 在 Servlet 类型的 Web 应用中生效
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(TimeZoneProperties.class)
@Import(JacksonTimeZoneConfig.class)
public class TimeZoneAutoConfiguration {

    /**
     * 注册 TimeZoneFilter，优先级最高
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

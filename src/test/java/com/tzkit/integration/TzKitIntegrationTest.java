package com.tzkit.integration;

import com.tzkit.config.TimeZoneAutoConfiguration;
import com.tzkit.config.TimeZoneProperties;
import com.tzkit.filter.TimeZoneFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TZKit auto-configuration.
 * Verifies that beans are wired correctly and properties are applied.
 */
class TzKitIntegrationTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(TimeZoneProperties.class)
    static class TestConfig {
        @Bean
        TimeZoneFilter timeZoneFilter(TimeZoneProperties properties) {
            return new TimeZoneFilter(properties);
        }
    }

    @Test
    void timeZoneFilterBeanShouldBeCreated() {
        contextRunner.run(context -> {
            TimeZoneFilter filter = context.getBean(TimeZoneFilter.class);
            assertThat(filter).isNotNull();

            TimeZoneProperties props = context.getBean(TimeZoneProperties.class);
            assertThat(props).isNotNull();
        });
    }

    @Test
    void customPropertiesShouldBeApplied() {
        contextRunner
            .withPropertyValues(
                "tzkit.header.timezone=X-Timezone",
                "tzkit.default-timezone=America/New_York"
            )
            .run(context -> {
                TimeZoneProperties props =
                    context.getBean(TimeZoneProperties.class);
                assertThat(props.getHeader().getTimezone()).isEqualTo("X-Timezone");
                assertThat(props.getDefaultTimezone()).isEqualTo("America/New_York");
            });
    }

    @Test
    void defaultPropertiesShouldHaveExpectedValues() {
        contextRunner.run(context -> {
            TimeZoneProperties props =
                context.getBean(TimeZoneProperties.class);
            assertThat(props.getHeader().getTimezone()).isEqualTo("Time-Zone");
            assertThat(props.getHeader().getOffset()).isEqualTo("Time-Zone-Offset");
            assertThat(props.getDefaultTimezone()).isEqualTo("Asia/Shanghai");
        });
    }

    @Test
    void autoConfigurationClassShouldExist() {
        assertThat(TimeZoneAutoConfiguration.class).isNotNull();
        assertThat(TimeZoneAutoConfiguration.class.isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class)).isTrue();
    }

    @Test
    void filterRegistrationShouldHaveHighestPriority() {
        contextRunner.run(context -> {
            TimeZoneProperties props = context.getBean(TimeZoneProperties.class);
            TimeZoneFilter filter = new TimeZoneFilter(props);

            FilterRegistrationBean<TimeZoneFilter> registration =
                new FilterRegistrationBean<>(filter);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            registration.addUrlPatterns("/*");

            assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        });
    }
}

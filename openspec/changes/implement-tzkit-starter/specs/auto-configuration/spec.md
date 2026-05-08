## ADDED Requirements

### Requirement: Auto-configuration class
The system SHALL provide TimeZoneAutoConfiguration class that automatically configures all starter components.

#### Scenario: Auto-configuration activates on web application
- **WHEN** Spring Boot application is a Servlet-based web application
- **THEN** TimeZoneAutoConfiguration SHALL be activated automatically

#### Scenario: Auto-configuration respects conditional annotations
- **WHEN** Spring Boot application is not a web application
- **THEN** TimeZoneAutoConfiguration SHALL NOT activate

### Requirement: Properties bean configuration
The system SHALL create TimeZoneProperties bean for configuration binding.

#### Scenario: Bind tzkit.* properties
- **WHEN** application.yml contains `tzkit.header.timezone: X-Timezone`
- **THEN** TimeZoneProperties.header.timezone SHALL be `X-Timezone`

#### Scenario: Use default values for missing properties
- **WHEN** no tzkit.* properties are configured
- **THEN** TimeZoneProperties SHALL use default values (`Time-Zone`, `Time-Zone-Offset`, `Asia/Shanghai`)

### Requirement: Filter registration
The system SHALL register TimeZoneFilter via FilterRegistrationBean.

#### Scenario: Register filter with high priority
- **WHEN** auto-configuration executes
- **THEN** TimeZoneFilter SHALL be registered with high order priority to execute before other filters

#### Scenario: Filter applies to all URLs
- **WHEN** TimeZoneFilter is registered
- **THEN** the filter SHALL apply to all request URLs (`/*`)

### Requirement: Jackson customization
The system SHALL apply JacksonTimeZoneConfig via Jackson2ObjectMapperBuilderCustomizer.

#### Scenario: Register custom serializers
- **WHEN** JacksonTimeZoneConfig customizer is applied
- **THEN** custom serializers for Date, LocalDateTime, Instant, LocalDate SHALL be registered

#### Scenario: Set ObjectMapper default timezone to UTC
- **WHEN** JacksonTimeZoneConfig customizer is applied
- **THEN** ObjectMapper default timezone SHALL be set to UTC

### Requirement: Auto-configuration imports file
The system SHALL provide META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports file.

#### Scenario: Register auto-configuration class
- **WHEN** starter is on classpath
- **THEN** Spring Boot SHALL discover TimeZoneAutoConfiguration via imports file

### Requirement: Conditional on class presence
The system SHALL use @ConditionalOnClass for dependency safety.

#### Scenario: Require Spring Web MVC
- **WHEN** DispatcherServlet class is not on classpath
- **THEN** auto-configuration SHALL NOT activate

#### Scenario: Require Jackson
- **WHEN** ObjectMapper class is not on classpath
- **THEN** auto-configuration SHALL NOT activate

### Requirement: Configuration processor metadata
The system SHALL generate configuration metadata for IDE support.

#### Scenario: Generate spring-configuration-metadata.json
- **WHEN** starter is compiled with spring-boot-configuration-processor
- **THEN** configuration metadata SHALL be generated for tzkit.* properties

#### Scenario: IDE shows property hints
- **WHEN** developer edits application.yml in IDE
- **THEN** IDE SHALL show autocomplete and documentation for tzkit.* properties
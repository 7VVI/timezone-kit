## 1. Project Setup

- [ ] 1.1 Create Maven module structure with pom.xml
- [ ] 1.2 Configure dependencies (Spring Boot 3.x, Jackson, Hutool 5.8.x)
- [ ] 1.3 Create META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports file
- [ ] 1.4 Add spring-boot-configuration-processor for metadata generation

## 2. Context Layer

- [ ] 2.1 Implement TimeZoneContext class with ThreadLocal holder
- [ ] 2.2 Add static get()/set()/clear() methods
- [ ] 2.3 Add getZoneId() accessor method
- [ ] 2.4 Ensure thread-safe implementation

## 3. Configuration Layer

- [ ] 3.1 Implement TimeZoneProperties with @ConfigurationProperties
- [ ] 3.2 Define properties: header.timezone, header.offset, default-timezone
- [ ] 3.3 Implement TimeZoneAutoConfiguration class
- [ ] 3.4 Add @ConditionalOnClass and @ConditionalOnWebApplication annotations
- [ ] 3.5 Implement JacksonTimeZoneConfig via Jackson2ObjectMapperBuilderCustomizer

## 4. Filter Layer

- [ ] 4.1 Implement TimeZoneFilter as Servlet Filter
- [ ] 4.2 Add timezone header parsing logic (IANA ID and UTC offset)
- [ ] 4.3 Implement timezone resolution priority (Time-Zone → Time-Zone-Offset → default)
- [ ] 4.4 Add ThreadLocal storage in doFilter()
- [ ] 4.5 Add ThreadLocal cleanup in afterCompletion()
- [ ] 4.6 Handle invalid timezone headers with fallback and logging
- [ ] 4.7 Register filter via FilterRegistrationBean in auto-configuration

## 5. Serialization Layer - Date

- [ ] 5.1 Implement DateSerializer for UTC → user timezone conversion
- [ ] 5.2 Implement DateDeserializer for user timezone → UTC conversion
- [ ] 5.3 Support @JsonFormat pattern override

## 6. Serialization Layer - LocalDateTime

- [ ] 6.1 Implement LocalDateTimeSerializer for UTC → user timezone conversion
- [ ] 6.2 Implement LocalDateTimeDeserializer for user timezone → UTC conversion
- [ ] 6.3 Support @JsonFormat pattern override

## 7. Serialization Layer - Instant

- [ ] 7.1 Implement InstantSerializer for ISO-8601 UTC format (no conversion)
- [ ] 7.2 Implement InstantDeserializer for ISO-8601 and timestamp formats

## 8. Serialization Layer - LocalDate

- [ ] 8.1 Implement LocalDateSerializer for yyyy-MM-dd format (no conversion)
- [ ] 8.2 Implement LocalDateDeserializer for yyyy-MM-dd parsing

## 9. Utility Layer

- [ ] 9.1 Implement TimeZoneUtils class with static utility methods
- [ ] 9.2 Add timezone access methods: getUserTimeZone(), getUserZoneId(), getUserZoneOffset(), getServerTimeZone()
- [ ] 9.3 Add current time methods: now(), today(), nowUtc(), nowInstant()
- [ ] 9.4 Add conversion methods: toUserZone(), toUtc(), convert()
- [ ] 9.5 Add formatting methods: format() with multiple overloads
- [ ] 9.6 Add parsing methods: parse() with multiple overloads
- [ ] 9.7 Implement DateUtils for multi-format date parsing

## 10. Annotation Layer (Optional)

- [ ] 10.1 Create @UserTZ annotation for controller parameters
- [ ] 10.2 Implement UserTZDateArgumentResolver
- [ ] 10.3 Register resolver in auto-configuration

## 11. Testing Layer

- [ ] 11.1 Unit tests for TimeZoneContext
- [ ] 11.2 Unit tests for TimeZoneFilter header parsing
- [ ] 11.3 Unit tests for each serializer/deserializer
- [ ] 11.4 Unit tests for TimeZoneUtils methods
- [ ] 11.5 Unit tests for DateUtils parsing
- [ ] 11.6 Integration test with @SpringBootTest + MockMvc for end-to-end flow
- [ ] 11.7 Test edge cases: missing header, invalid timezone, offset with minutes

## 12. Documentation & Finalization

- [ ] 12.1 Add README.md with usage instructions
- [ ] 12.2 Document configuration properties
- [ ] 12.3 Add code examples for common use cases
- [ ] 12.4 Verify configuration metadata generation
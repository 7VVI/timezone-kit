# TZKit Spring Boot Starter - Design Spec

## Overview

A Spring Boot 3.x starter module for automatic timezone conversion and time utility. It intercepts HTTP requests to extract the user's timezone, stores it in a ThreadLocal context, and automatically converts dates between UTC (backend) and the user's timezone (frontend) during JSON serialization/deserialization. Also provides a comprehensive `TimeZoneUtils` utility class.

## Requirements

- Java 17+
- Spring Boot 3.x
- Jackson (spring-boot-starter-json)
- Hutool 5.8.x (for FastDateFormat)

## Maven Coordinates

```xml
<groupId>com.tzkit</groupId>
<artifactId>tzkit-spring-boot-starter</artifactId>
<version>1.0.0</version>
```

## Architecture

### Approach: Jackson Global Customization + Filter/ThreadLocal

- **Jackson serializers/deserializers** handle automatic timezone conversion during JSON request/response processing
- **Servlet Filter + ThreadLocal** makes the user's timezone available to any layer (not just Jackson)
- **Utility class** reads timezone from ThreadLocal for ad-hoc operations

### Data Flow

```
Frontend Request
  │
  ├─ Header: Time-Zone: Asia/Shanghai
  │  OR Header: Time-Zone-Offset: +8
  │
  ▼
TimeZoneFilter (Servlet Filter)
  │  Parse header → Store in TimeZoneContext (ThreadLocal)
  │
  ▼
Jackson Deserialization (Request Body)
  │  Date/LocalDateTime deserializers
  │  Read user timezone from TimeZoneContext
  │  "2026-01-22 10:00:00" (user TZ) → Date/LocalDateTime (UTC)
  │
  ▼
Controller / Service Layer
  │  Business code receives UTC time
  │  Can use TimeZoneUtils for additional operations
  │
  ▼
Jackson Serialization (Response Body)
  │  Date/LocalDateTime serializers
  │  Read user timezone from TimeZoneContext
  │  Date/LocalDateTime (UTC) → "2026-01-22 18:00:00" (user TZ)
  │
  ▼
TimeZoneFilter.afterCompletion()
  │  Clear ThreadLocal to prevent memory leaks
  │
  ▼
Frontend receives time in their timezone
```

## Timezone Resolution Strategy

The filter supports two header formats:

**Format 1: IANA Timezone ID**
```
Time-Zone: Asia/Shanghai
```

**Format 2: UTC Offset Hours**
```
Time-Zone-Offset: +8
Time-Zone-Offset: -5
Time-Zone-Offset: +5:30
```

Resolution priority:
1. Check `Time-Zone` header → use directly
2. If absent, check `Time-Zone-Offset` → convert to `GMT±HH:mm`
3. Both absent → fallback to `Asia/Shanghai`

Header names and default timezone are configurable via `tzkit.*` properties.

## Project Structure

```
timezone-kit/
├── pom.xml
├── src/main/java/com/tzkit/
│   ├── config/
│   │   ├── TimeZoneAutoConfiguration.java    # Auto-config entry point
│   │   ├── JacksonTimeZoneConfig.java        # ObjectMapper customization
│   │   └── TimeZoneProperties.java           # Configuration properties
│   ├── context/
│   │   └── TimeZoneContext.java              # ThreadLocal timezone holder
│   ├── filter/
│   │   └── TimeZoneFilter.java              # Servlet Filter
│   ├── serializer/
│   │   ├── DateSerializer.java              # Date → JSON
│   │   ├── DateDeserializer.java            # JSON → Date
│   │   ├── LocalDateTimeSerializer.java     # LocalDateTime → JSON
│   │   ├── LocalDateTimeDeserializer.java   # JSON → LocalDateTime
│   │   ├── InstantSerializer.java           # Instant → JSON
│   │   ├── InstantDeserializer.java         # JSON → Instant
│   │   ├── LocalDateSerializer.java         # LocalDate → JSON
│   │   └── LocalDateDeserializer.java       # JSON → LocalDate
│   ├── utils/
│   │   ├── TimeZoneUtils.java               # Core timezone utility
│   │   └── DateUtils.java                   # Multi-format date parser
│   └── annotation/
│       └── UserTZ.java                      # Custom annotation
├── src/main/resources/
│   └── META-INF/spring/
│       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── src/test/java/com/tzkit/
    └── ...test classes
```

## Configuration Properties

Prefix: `tzkit`

| Property | Default | Description |
|----------|---------|-------------|
| `tzkit.header.timezone` | `Time-Zone` | Request header name for timezone ID |
| `tzkit.header.offset` | `Time-Zone-Offset` | Request header name for UTC offset |
| `tzkit.default-timezone` | `Asia/Shanghai` | Fallback timezone when no header provided |

## Serialization/Deserialization Strategy

| Java Type | Serialization (→ JSON) | Deserialization (JSON →) | Timezone Conversion |
|-----------|----------------------|-------------------------|---------------------|
| `Date` | UTC Date → user TZ string | user TZ string → UTC Date | Yes |
| `LocalDateTime` | UTC LDT → user TZ string | user TZ string → UTC LDT | Yes |
| `Instant` | ISO-8601 UTC (e.g., `2026-01-22T10:00:00Z`) | ISO/timestamp → Instant | No (always UTC) |
| `LocalDate` | `yyyy-MM-dd` | `yyyy-MM-dd` → LocalDate | No (date-only) |
| `LocalTime` | `HH:mm:ss` | `HH:mm:ss` → LocalTime | No (time-only) |

All types support `@JsonFormat(pattern=...)` for custom format override.

## TimeZoneUtils API

```java
public final class TimeZoneUtils {

    // ===== Timezone Access =====

    /** Get current request's user timezone */
    static TimeZone getUserTimeZone();

    /** Get current request's user ZoneId */
    static ZoneId getUserZoneId();

    /** Get user timezone offset, e.g., +08:00 */
    static ZoneOffset getUserZoneOffset();

    /** Get server default timezone (UTC) */
    static TimeZone getServerTimeZone();

    // ===== Current Time =====

    /** Get current time in user timezone */
    static LocalDateTime now();

    /** Get current date in user timezone */
    static LocalDate today();

    /** Get current UTC time */
    static LocalDateTime nowUtc();

    /** Get current instant */
    static Instant nowInstant();

    // ===== Timezone Conversion =====

    /** UTC → User timezone */
    static LocalDateTime toUserZone(LocalDateTime utcTime);

    /** User timezone → UTC */
    static LocalDateTime toUtc(LocalDateTime userTime);

    /** UTC Date → User timezone Date */
    static Date toUserZone(Date utcDate);

    /** User timezone Date → UTC Date */
    static Date toUtc(Date userDate);

    /** Instant → User timezone LocalDateTime */
    static LocalDateTime toUserZone(Instant instant);

    /** Convert between any two timezones */
    static LocalDateTime convert(LocalDateTime time, ZoneId from, ZoneId to);

    // ===== Formatting =====

    /** Format with user timezone */
    static String format(LocalDateTime time, String pattern);

    /** Format with user timezone, default yyyy-MM-dd HH:mm:ss */
    static String format(LocalDateTime time);

    /** Format with specific timezone */
    static String format(LocalDateTime time, String pattern, ZoneId zoneId);

    // ===== Parsing =====

    /** Parse with user timezone */
    static LocalDateTime parse(String text);

    /** Parse with pattern and user timezone */
    static LocalDateTime parse(String text, String pattern);
}
```

## Core Components

### TimeZoneContext

ThreadLocal-based holder for the current request's user timezone. Provides static `get()`/`set()`/`clear()` methods. Automatically cleaned up by the filter's `afterCompletion()`.

### TimeZoneFilter

Servlet Filter registered via `FilterRegistrationBean` in auto-configuration:
1. Reads `Time-Zone` or `Time-Zone-Offset` header
2. Resolves to a `TimeZone` object
3. Stores in `TimeZoneContext`
4. In `afterCompletion()`, clears the ThreadLocal

### JacksonTimeZoneConfig

Registers custom serializers/deserializers via `Jackson2ObjectMapperBuilderCustomizer`:
- Sets `ObjectMapper` default timezone to UTC
- Registers `JavaTimeModule` with custom serializers/deserializers for Date, LocalDateTime, Instant, LocalDate

### DateUtils

Multi-format date parser supporting:
- Numeric formats: `yyyyMMddHHmmss`, `yyyyMMdd`, timestamps
- Standard formats: `yyyy-MM-dd`, `yyyy-MM-dd HH:mm:ss`, etc.
- Uses Hutool's `FastDateFormat` with timezone awareness

### UserTZ Annotation + ArgumentResolver

Optional annotation `@UserTZ` for Spring MVC controller parameters. Works with `@RequestParam` to parse date query parameters using the user's timezone.

## Auto-Configuration

Registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
com.tzkit.config.TimeZoneAutoConfiguration
```

The auto-configuration:
1. Creates `TimeZoneProperties` bean
2. Registers `TimeZoneFilter` via `FilterRegistrationBean`
3. Applies `JacksonTimeZoneConfig` via `Jackson2ObjectMapperBuilderCustomizer`
4. Registers `UserTZDateArgumentResolver` (if web MVC present)
5. Uses `@ConditionalOnClass` / `@ConditionalOnWebApplication` for safety

## Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-json</artifactId>
    </dependency>

    <!-- Jackson -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>

    <!-- Hutool -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-core</artifactId>
        <version>5.8.34</version>
    </dependency>

    <!-- Configuration Processor -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Testing Strategy

- Unit tests for `TimeZoneUtils`, `DateUtils`, `TimeZoneContext`
- Unit tests for each serializer/deserializer using Jackson's `ObjectMapper`
- Integration test with `@SpringBootTest` + MockMvc to verify end-to-end filter → serialization flow
- Edge cases: missing header, invalid timezone, offset with minutes, no request context

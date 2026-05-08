## Why

Frontend-backend timezone mismatch is a common pain point in web applications. Backend stores dates in UTC, but users expect to see and input times in their local timezone. Current solutions require manual timezone conversion in each controller/service, leading to boilerplate code and inconsistent behavior.

TZKit Spring Boot Starter solves this by providing automatic timezone conversion at the serialization layer - developers write UTC-based business logic, and the starter handles all conversions transparently.

## What Changes

Create a complete Spring Boot 3.x starter module (`tzkit-spring-boot-starter`) that provides:

- **Servlet Filter**: Extracts timezone from request headers (`Time-Zone` or `Time-Zone-Offset`)
- **ThreadLocal Context**: `TimeZoneContext` holds user timezone for the request lifecycle
- **Jackson Serializers/Deserializers**: Automatic UTC ↔ user timezone conversion for `Date`, `LocalDateTime`, `Instant`, `LocalDate`
- **TimeZoneUtils**: Comprehensive utility class for timezone-aware operations
- **Auto-Configuration**: Zero-config setup via Spring Boot auto-configuration mechanism
- **Configuration Properties**: Customizable header names and default timezone via `tzkit.*` prefix

## Capabilities

### New Capabilities

- `timezone-context`: ThreadLocal-based timezone holder with request lifecycle management
- `timezone-filter`: Servlet filter that extracts timezone from request headers (IANA ID or UTC offset)
- `jackson-serialization`: Custom Jackson serializers/deserializers for automatic timezone conversion
- `timezone-utils`: Utility class providing timezone-aware date operations (now, convert, format, parse)
- `auto-configuration`: Spring Boot auto-configuration for zero-config integration

### Modified Capabilities

None - this is a new standalone starter module.

## Impact

**New Module**: Creates complete starter project structure under `timezone-kit/`

**Dependencies Added**:
- Spring Boot 3.x (starter-web, starter-json)
- Jackson (databind, datatype-jsr310)
- Hutool 5.8.x (for FastDateFormat)

**Files Created** (estimated 20+ source files):
- Configuration layer: `TimeZoneAutoConfiguration`, `JacksonTimeZoneConfig`, `TimeZoneProperties`
- Context layer: `TimeZoneContext`
- Filter layer: `TimeZoneFilter`
- Serialization layer: 8 serializer/deserializer classes
- Utility layer: `TimeZoneUtils`, `DateUtils`
- Annotation layer: `@UserTZ` (optional feature)
- Test layer: Unit tests for each component + integration tests

**API Exposure**:
- `TimeZoneUtils` static methods for ad-hoc timezone operations
- `@UserTZ` annotation for controller parameter parsing
- Configuration properties under `tzkit.*` prefix
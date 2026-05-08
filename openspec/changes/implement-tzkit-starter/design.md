## Context

**Background**: Web applications serving global users face timezone complexity. Backend typically stores timestamps in UTC for consistency, while users expect to see and input times in their local timezone. Manual timezone conversion in every controller/service is tedious and error-prone.

**Current State**: This is a new starter module - no existing implementation to migrate from.

**Constraints**:
- Must work with Spring Boot 3.x and Java 17+
- Must integrate seamlessly with Jackson's serialization pipeline
- ThreadLocal must be properly cleaned to prevent memory leaks
- Must support both IANA timezone IDs and UTC offset formats

**Stakeholders**: Application developers using this starter, frontend developers setting timezone headers

## Goals / Non-Goals

**Goals:**
- Zero-config integration: Spring Boot auto-configuration with sensible defaults
- Transparent timezone conversion at JSON serialization layer
- Request-scoped timezone context available to all application layers
- Comprehensive timezone utility API for ad-hoc operations
- Thread-safe ThreadLocal management with automatic cleanup

**Non-Goals:**
- Database timezone conversion (this starter handles only JSON serialization/deserialization)
- Timezone detection from browser/client IP (requires external header)
- Custom timezone per field/property (use `@JsonFormat` for field-level overrides)
- Support for legacy Java date types (java.sql.Date, java.sql.Timestamp)

## Decisions

### D1: ThreadLocal vs Request Attribute for Timezone Storage

**Decision**: ThreadLocal (`TimeZoneContext`)

**Alternatives Considered**:
- Request Attribute (`HttpServletRequest.setAttribute`): Simpler but only accessible in web layer
- SecurityContext (Spring Security): Only available if Spring Security is present

**Rationale**: ThreadLocal provides timezone to any layer (service, repository, utility) without requiring web context. Works with async operations that preserve thread context. Similar pattern used by `TransactionContextHolder` in Spring.

### D2: Filter vs Interceptor for Timezone Extraction

**Decision**: Servlet Filter (`TimeZoneFilter`)

**Alternatives Considered**:
- HandlerInterceptor: Only intercepts controller calls, misses static resources and error handling
- WebFilter (WebFlux): Not applicable - this starter targets Servlet-based Spring MVC

**Rationale**: Filter executes before any Spring MVC processing, ensuring timezone is available for Jackson deserialization. `afterCompletion()` guarantees ThreadLocal cleanup even on exceptions.

### D3: Jackson Customization Approach

**Decision**: `Jackson2ObjectMapperBuilderCustomizer` with custom serializers/deserializers

**Alternatives Considered**:
- `@JsonFormat` on each field: Requires manual annotation everywhere
- Global `ObjectMapper` bean replacement: Breaks other customizations
- `JsonSerializer`/`JsonDeserializer` per type: Most granular control

**Rationale**: Customizer pattern integrates with existing ObjectMapper configuration. Custom serializers/deserializers provide type-specific timezone logic while allowing `@JsonFormat` override.

### D4: Timezone Resolution Priority

**Decision**: `Time-Zone` header → `Time-Zone-Offset` header → Default (`Asia/Shanghai`)

**Alternatives Considered**:
- Require only IANA ID: Less flexible for simple offset use cases
- Use system timezone as default: Inconsistent across server environments

**Rationale**: IANA ID provides full timezone rules (DST, historical changes). Offset is simpler for basic use cases. Configurable default allows project-specific fallback.

### D5: UTC vs Server Default as Backend Base

**Decision**: UTC as backend base timezone

**Alternatives Considered**:
- Server timezone: Consistent within one deployment, but breaks in distributed/multi-region setups

**Rationale**: UTC is timezone-agnostic, works correctly across distributed deployments. ObjectMapper default timezone set to UTC ensures consistent behavior.

### D6: Instant Serialization Strategy

**Decision**: ISO-8601 UTC format (no timezone conversion)

**Alternatives Considered**:
- Convert to user timezone: Breaks Instant's design as "point on timeline in UTC"
- Timestamp (epoch millis): Less human-readable

**Rationale**: `Instant` represents a precise moment in UTC. Converting would lose this semantic. ISO-8601 format (`2026-01-22T10:00:00Z`) clearly indicates UTC.

## Risks / Trade-offs

**R1: ThreadLocal Memory Leak**
- Risk: ThreadLocal not cleared if filter fails or async operations don't propagate context
- Mitigation: `TimeZoneFilter.afterCompletion()` always clears ThreadLocal. Use `DelegatingSecurityContextRunnable` pattern for async if needed.

**R2: Async/Scheduled Tasks Without Request Context**
- Risk: Background tasks calling `TimeZoneUtils` will get default timezone, not user-specific timezone
- Mitigation: Document that `TimeZoneUtils` is request-scoped. For background tasks, pass timezone explicitly or use `TimeZoneContext.set()` manually.

**R3: Invalid Timezone Header**
- Risk: Malformed timezone header causes errors
- Mitigation: Filter validates timezone before storing. Invalid values fall back to default with logging.

**R4: Performance Impact of Per-Request Conversion**
- Risk: Serialization/deserialization overhead for large JSON payloads
- Mitigation: Jackson serializers use pre-computed `ZoneId` from ThreadLocal, no per-field lookup. Minimal overhead compared to manual conversion.

**R5: Spring Boot 3.x Only**
- Risk: Projects on Spring Boot 2.x cannot use this starter
- Mitigation: Document version requirements clearly. Java 17+ and Spring Boot 3.x are industry standard now.

## Migration Plan

**Deployment**: This is a new module, no migration required.

**Rollback**: Not applicable - starter can be removed from project dependencies if issues arise.

**Configuration**:
1. Add starter dependency to project pom.xml
2. Frontend adds `Time-Zone` or `Time-Zone-Offset` header to requests
3. Optionally configure `tzkit.*` properties for custom header names or default timezone

## Open Questions

None - design document provides complete technical decisions for implementation.
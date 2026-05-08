## ADDED Requirements

### Requirement: Timezone header extraction
The system SHALL extract timezone information from HTTP request headers.

#### Scenario: Extract IANA timezone ID
- **WHEN** request contains header `Time-Zone: Asia/Shanghai`
- **THEN** the system SHALL parse the IANA ID and create a TimeZone object for `Asia/Shanghai`

#### Scenario: Extract UTC offset hours
- **WHEN** request contains header `Time-Zone-Offset: +8`
- **THEN** the system SHALL parse the offset and create a TimeZone object for `GMT+08:00`

#### Scenario: Extract UTC offset with minutes
- **WHEN** request contains header `Time-Zone-Offset: +5:30`
- **THEN** the system SHALL parse the offset and create a TimeZone object for `GMT+05:30`

#### Scenario: Extract negative UTC offset
- **WHEN** request contains header `Time-Zone-Offset: -5`
- **THEN** the system SHALL parse the offset and create a TimeZone object for `GMT-05:00`

### Requirement: Timezone resolution priority
The system SHALL follow a defined priority order for timezone resolution from headers.

#### Scenario: Prefer IANA ID over offset
- **WHEN** request contains both `Time-Zone: Asia/Shanghai` and `Time-Zone-Offset: +8`
- **THEN** the system SHALL use the `Time-Zone` header value (Asia/Shanghai)

#### Scenario: Use offset when IANA ID absent
- **WHEN** request contains only `Time-Zone-Offset: +8` and no `Time-Zone` header
- **THEN** the system SHALL use the offset value

#### Scenario: Use default when both headers absent
- **WHEN** request contains neither `Time-Zone` nor `Time-Zone-Offset` header
- **THEN** the system SHALL use the configured default timezone (`Asia/Shanghai`)

### Requirement: ThreadLocal storage from filter
The system SHALL store the resolved timezone in TimeZoneContext before request processing.

#### Scenario: Store timezone before controller
- **WHEN** filter processes a request with timezone header
- **THEN** the timezone SHALL be stored in TimeZoneContext before any controller method executes

#### Scenario: Store default timezone for headerless requests
- **WHEN** filter processes a request without timezone headers
- **THEN** the default timezone SHALL be stored in TimeZoneContext

### Requirement: ThreadLocal cleanup after request
The system SHALL clear TimeZoneContext after request completion to prevent memory leaks.

#### Scenario: Clear context after successful request
- **WHEN** request completes successfully
- **THEN** TimeZoneContext SHALL be cleared in filter's afterCompletion callback

#### Scenario: Clear context after exception
- **WHEN** request throws an exception
- **THEN** TimeZoneContext SHALL still be cleared in filter's afterCompletion callback

#### Scenario: Clear context after error response
- **WHEN** request results in an error response (4xx/5xx)
- **THEN** TimeZoneContext SHALL be cleared in filter's afterCompletion callback

### Requirement: Invalid timezone header handling
The system SHALL handle invalid timezone header values gracefully without causing request failures.

#### Scenario: Invalid IANA timezone ID
- **WHEN** request contains header `Time-Zone: Invalid/Zone`
- **THEN** the system SHALL fall back to default timezone and log a warning

#### Scenario: Malformed offset format
- **WHEN** request contains header `Time-Zone-Offset: abc`
- **THEN** the system SHALL fall back to default timezone and log a warning

### Requirement: Configurable header names
The system SHALL support configurable header names via Spring Boot properties.

#### Scenario: Custom timezone header name
- **WHEN** property `tzkit.header.timezone=X-Timezone` is set
- **THEN** the filter SHALL read timezone from header `X-Timezone` instead of `Time-Zone`

#### Scenario: Custom offset header name
- **WHEN** property `tzkit.header.offset=X-Offset` is set
- **THEN** the filter SHALL read offset from header `X-Offset` instead of `Time-Zone-Offset`

### Requirement: Configurable default timezone
The system SHALL support configurable default timezone via Spring Boot property.

#### Scenario: Custom default timezone
- **WHEN** property `tzkit.default-timezone=America/New_York` is set
- **THEN** the filter SHALL use `America/New_York` as fallback when headers are absent
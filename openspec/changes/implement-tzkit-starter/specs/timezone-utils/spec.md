## ADDED Requirements

### Requirement: Get user timezone
The system SHALL provide TimeZoneUtils.getUserTimeZone() to retrieve the current request's user timezone.

#### Scenario: Return timezone from context
- **WHEN** TimeZoneUtils.getUserTimeZone() is called during a request with TimeZoneContext set
- **THEN** the system SHALL return the TimeZone object stored in context

#### Scenario: Return null when context empty
- **WHEN** TimeZoneUtils.getUserTimeZone() is called when TimeZoneContext is not set
- **THEN** the system SHALL return null

### Requirement: Get user ZoneId
The system SHALL provide TimeZoneUtils.getUserZoneId() to retrieve the current request's user ZoneId.

#### Scenario: Return ZoneId from context
- **WHEN** TimeZoneUtils.getUserZoneId() is called during a request with TimeZoneContext set
- **THEN** the system SHALL return the ZoneId corresponding to the stored timezone

### Requirement: Get user timezone offset
The system SHALL provide TimeZoneUtils.getUserZoneOffset() to retrieve the timezone offset.

#### Scenario: Return offset for user timezone
- **WHEN** TimeZoneUtils.getUserZoneOffset() is called with TimeZoneContext containing `Asia/Shanghai`
- **THEN** the system SHALL return `ZoneOffset.of("+08:00")`

### Requirement: Get server timezone
The system SHALL provide TimeZoneUtils.getServerTimeZone() to retrieve the server's default timezone (UTC).

#### Scenario: Return UTC timezone
- **WHEN** TimeZoneUtils.getServerTimeZone() is called
- **THEN** the system SHALL return TimeZone.getTimeZone("UTC")

### Requirement: Current time operations
The system SHALL provide methods to get current time in user timezone or UTC.

#### Scenario: Get now in user timezone
- **WHEN** TimeZoneUtils.now() is called with TimeZoneContext containing `Asia/Shanghai`
- **THEN** the system SHALL return LocalDateTime representing current time in Asia/Shanghai timezone

#### Scenario: Get today in user timezone
- **WHEN** TimeZoneUtils.today() is called with TimeZoneContext containing `Asia/Shanghai`
- **THEN** the system SHALL return LocalDate representing today's date in Asia/Shanghai timezone

#### Scenario: Get now in UTC
- **WHEN** TimeZoneUtils.nowUtc() is called
- **THEN** the system SHALL return LocalDateTime representing current time in UTC

#### Scenario: Get current instant
- **WHEN** TimeZoneUtils.nowInstant() is called
- **THEN** the system SHALL return Instant representing current moment in UTC

### Requirement: UTC to user timezone conversion
The system SHALL provide methods to convert UTC time to user timezone.

#### Scenario: Convert LocalDateTime UTC to user timezone
- **WHEN** TimeZoneUtils.toUserZone(LocalDateTime.parse("2026-01-22T10:00:00")) is called
- **AND** TimeZoneContext contains `Asia/Shanghai` (UTC+8)
- **THEN** the system SHALL return `2026-01-22T18:00:00`

#### Scenario: Convert Date UTC to user timezone
- **WHEN** TimeZoneUtils.toUserZone(Date) is called with a UTC Date `2026-01-22 10:00:00 UTC`
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return a Date representing `2026-01-22 18:00:00 Asia/Shanghai`

#### Scenario: Convert Instant to user timezone LocalDateTime
- **WHEN** TimeZoneUtils.toUserZone(Instant.parse("2026-01-22T10:00:00Z")) is called
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return LocalDateTime `2026-01-22T18:00:00`

### Requirement: User timezone to UTC conversion
The system SHALL provide methods to convert user timezone time to UTC.

#### Scenario: Convert LocalDateTime user timezone to UTC
- **WHEN** TimeZoneUtils.toUtc(LocalDateTime.parse("2026-01-22T18:00:00")) is called
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return `2026-01-22T10:00:00` (UTC)

#### Scenario: Convert Date user timezone to UTC
- **WHEN** TimeZoneUtils.toUtc(Date) is called with a Date in user timezone
- **THEN** the system SHALL return a Date representing UTC equivalent

### Requirement: Arbitrary timezone conversion
The system SHALL provide TimeZoneUtils.convert() for conversion between any two timezones.

#### Scenario: Convert between arbitrary timezones
- **WHEN** TimeZoneUtils.convert(LocalDateTime.parse("2026-01-22T10:00:00"), ZoneId.of("UTC"), ZoneId.of("America/New_York")) is called
- **THEN** the system SHALL return `2026-01-22T05:00:00` (EST, UTC-5)

### Requirement: Formatting with user timezone
The system SHALL provide TimeZoneUtils.format() methods to format dates using user timezone.

#### Scenario: Format with default pattern
- **WHEN** TimeZoneUtils.format(LocalDateTime.parse("2026-01-22T10:00:00")) is called
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return `"2026-01-22 18:00:00"`

#### Scenario: Format with custom pattern
- **WHEN** TimeZoneUtils.format(LocalDateTime.parse("2026-01-22T10:00:00"), "yyyy/MM/dd HH:mm") is called
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return `"2026/01/22 18:00"`

#### Scenario: Format with specific timezone
- **WHEN** TimeZoneUtils.format(LocalDateTime.parse("2026-01-22T10:00:00"), "yyyy-MM-dd HH:mm:ss", ZoneId.of("America/New_York")) is called
- **THEN** the system SHALL format using America/New_York timezone

### Requirement: Parsing with user timezone
The system SHALL provide TimeZoneUtils.parse() methods to parse strings using user timezone.

#### Scenario: Parse with default format
- **WHEN** TimeZoneUtils.parse("2026-01-22 18:00:00") is called
- **AND** TimeZoneContext contains `Asia/Shanghai`
- **THEN** the system SHALL return LocalDateTime `2026-01-22T18:00:00` interpreted in user timezone

#### Scenario: Parse with custom pattern
- **WHEN** TimeZoneUtils.parse("2026/01/22 18:00", "yyyy/MM/dd HH:mm") is called
- **THEN** the system SHALL parse using the custom pattern and user timezone
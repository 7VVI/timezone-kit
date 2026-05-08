## ADDED Requirements

### Requirement: Date serialization with timezone conversion
The system SHALL serialize `java.util.Date` objects to JSON strings using the user's timezone.

#### Scenario: Serialize UTC Date to user timezone
- **WHEN** a Date object representing UTC time `2026-01-22 10:00:00 UTC` is serialized
- **AND** TimeZoneContext contains `Asia/Shanghai` (UTC+8)
- **THEN** the JSON output SHALL be `"2026-01-22 18:00:00"` (user timezone representation)

#### Scenario: Serialize Date with custom pattern
- **WHEN** a Date field is annotated with `@JsonFormat(pattern="yyyy-MM-dd")`
- **THEN** the serialization SHALL use the custom pattern instead of default format

### Requirement: Date deserialization with timezone conversion
The system SHALL deserialize JSON date strings to `java.util.Date` objects in UTC, converting from user's timezone.

#### Scenario: Deserialize user timezone string to UTC Date
- **WHEN** JSON contains `"2026-01-22 18:00:00"`
- **AND** TimeZoneContext contains `Asia/Shanghai` (UTC+8)
- **THEN** the resulting Date SHALL represent UTC time `2026-01-22 10:00:00 UTC`

#### Scenario: Deserialize with custom pattern
- **WHEN** a Date field is annotated with `@JsonFormat(pattern="yyyy/MM/dd HH:mm")`
- **THEN** the deserialization SHALL parse using the custom pattern

### Requirement: LocalDateTime serialization with timezone conversion
The system SHALL serialize `java.time.LocalDateTime` objects to JSON strings using the user's timezone.

#### Scenario: Serialize UTC LocalDateTime to user timezone
- **WHEN** a LocalDateTime object representing UTC time `2026-01-22T10:00:00` is serialized
- **AND** TimeZoneContext contains `Asia/Shanghai` (UTC+8)
- **THEN** the JSON output SHALL be `"2026-01-22 18:00:00"`

#### Scenario: Serialize LocalDateTime with custom format
- **WHEN** a LocalDateTime field is annotated with `@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm")`
- **THEN** the serialization SHALL use the custom pattern

### Requirement: LocalDateTime deserialization with timezone conversion
The system SHALL deserialize JSON datetime strings to `java.time.LocalDateTime` objects in UTC, converting from user's timezone.

#### Scenario: Deserialize user timezone string to UTC LocalDateTime
- **WHEN** JSON contains `"2026-01-22 18:00:00"`
- **AND** TimeZoneContext contains `Asia/Shanghai` (UTC+8)
- **THEN** the resulting LocalDateTime SHALL be `2026-01-22T10:00:00` (UTC)

### Requirement: Instant serialization without timezone conversion
The system SHALL serialize `java.time.Instant` objects to ISO-8601 UTC format without timezone conversion.

#### Scenario: Serialize Instant to ISO-8601 UTC
- **WHEN** an Instant object `2026-01-22T10:00:00Z` is serialized
- **THEN** the JSON output SHALL be `"2026-01-22T10:00:00Z"` (UTC representation)

#### Scenario: Instant ignores user timezone
- **WHEN** an Instant is serialized with TimeZoneContext set to any timezone
- **THEN** the output SHALL still be UTC ISO-8601 format

### Requirement: Instant deserialization from multiple formats
The system SHALL deserialize JSON to `java.time.Instant` from ISO-8601 strings or numeric timestamps.

#### Scenario: Deserialize ISO-8601 string to Instant
- **WHEN** JSON contains `"2026-01-22T10:00:00Z"`
- **THEN** the resulting Instant SHALL be `2026-01-22T10:00:00Z`

#### Scenario: Deserialize numeric timestamp to Instant
- **WHEN** JSON contains `1737537600000` (epoch millis)
- **THEN** the resulting Instant SHALL represent the correct UTC moment

### Requirement: LocalDate serialization without timezone conversion
The system SHALL serialize `java.time.LocalDate` objects to JSON strings in `yyyy-MM-dd` format without timezone conversion.

#### Scenario: Serialize LocalDate to ISO date
- **WHEN** a LocalDate object `2026-01-22` is serialized
- **THEN** the JSON output SHALL be `"2026-01-22"`

#### Scenario: LocalDate ignores timezone
- **WHEN** a LocalDate is serialized with TimeZoneContext set
- **THEN** the output SHALL be the same date string regardless of timezone

### Requirement: LocalDate deserialization from date strings
The system SHALL deserialize JSON date strings to `java.time.LocalDate` objects.

#### Scenario: Deserialize ISO date string
- **WHEN** JSON contains `"2026-01-22"`
- **THEN** the resulting LocalDate SHALL be `2026-01-22`

### Requirement: ObjectMapper default timezone configuration
The system SHALL configure ObjectMapper's default timezone to UTC for consistent behavior.

#### Scenario: ObjectMapper uses UTC as default
- **WHEN** ObjectMapper is configured by this starter
- **THEN** the default timezone SHALL be set to UTC

### Requirement: @JsonFormat override support
The system SHALL allow `@JsonFormat` annotation to override default serialization/deserialization behavior.

#### Scenario: @JsonFormat timezone override
- **WHEN** a field is annotated with `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="America/New_York")`
- **THEN** the serialization/deserialization SHALL use the specified timezone instead of user timezone

#### Scenario: @JsonFormat pattern override
- **WHEN** a field is annotated with `@JsonFormat(pattern="MM/dd/yyyy")`
- **THEN** the serialization SHALL use the specified pattern
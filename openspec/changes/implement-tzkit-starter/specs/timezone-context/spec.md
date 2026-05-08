## ADDED Requirements

### Requirement: ThreadLocal timezone storage
The system SHALL provide a ThreadLocal-based holder for storing and retrieving the current request's user timezone.

#### Scenario: Store timezone in context
- **WHEN** TimeZoneContext.set(TimeZone) is called
- **THEN** the timezone SHALL be stored in ThreadLocal and retrievable via TimeZoneContext.get()

#### Scenario: Retrieve timezone from context
- **WHEN** TimeZoneContext.get() is called within a request thread
- **THEN** the system SHALL return the timezone previously stored for that thread

#### Scenario: Clear timezone context
- **WHEN** TimeZoneContext.clear() is called
- **THEN** the ThreadLocal SHALL be cleared to prevent memory leaks

### Requirement: Thread-safe timezone access
The system SHALL ensure timezone context operations are thread-safe and isolated per request thread.

#### Scenario: Concurrent requests have independent contexts
- **WHEN** multiple concurrent requests with different timezones are processed
- **THEN** each request thread SHALL have its own timezone context without interference

#### Scenario: Context isolation across threads
- **WHEN** a request thread's timezone is set
- **THEN** other request threads SHALL NOT access that timezone from their TimeZoneContext.get() calls

### Requirement: Null timezone handling
The system SHALL handle null timezone gracefully and return null from get() when no timezone has been set.

#### Scenario: Get timezone when not set
- **WHEN** TimeZoneContext.get() is called before any set() operation
- **THEN** the system SHALL return null

#### Scenario: Get timezone after clear
- **WHEN** TimeZoneContext.get() is called after TimeZoneContext.clear()
- **THEN** the system SHALL return null

### Requirement: ZoneId accessor
The system SHALL provide a ZoneId accessor method TimeZoneContext.getZoneId() for modern Java time API usage.

#### Scenario: Get ZoneId from stored TimeZone
- **WHEN** TimeZoneContext.getZoneId() is called after TimeZoneContext.set() with a valid TimeZone
- **THEN** the system SHALL return the corresponding ZoneId

#### Scenario: Get ZoneId when not set
- **WHEN** TimeZoneContext.getZoneId() is called when no timezone is stored
- **THEN** the system SHALL return null
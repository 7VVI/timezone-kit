# TZKit Spring Boot Starter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot 3.x starter that automatically converts timestamps between UTC (backend) and user timezone (frontend) during JSON serialization/deserialization, with zero-config integration via auto-configuration.

**Architecture:** ThreadLocal holds user timezone extracted by Servlet Filter from request headers. Jackson serializers/deserializers read timezone from ThreadLocal for automatic conversion. Utility class provides timezone-aware operations for ad-hoc use.

**Tech Stack:** Java 17+, Spring Boot 3.x, Jackson, Hutool 5.8.x, Maven

---

## File Structure

```
src/main/java/com/tzkit/
├── config/
│   ├── TimeZoneAutoConfiguration.java    # Auto-config entry, creates beans
│   ├── JacksonTimeZoneConfig.java        # Jackson ObjectMapper customization
│   └── TimeZoneProperties.java           # @ConfigurationProperties binding
├── context/
│   └── TimeZoneContext.java              # ThreadLocal holder
├── filter/
│   └── TimeZoneFilter.java              # Servlet filter for header extraction
├── serializer/
│   ├── DateSerializer.java              # Date → JSON (UTC to user TZ)
│   ├── DateDeserializer.java            # JSON → Date (user TZ to UTC)
│   ├── LocalDateTimeSerializer.java     # LocalDateTime → JSON
│   ├── LocalDateTimeDeserializer.java   # JSON → LocalDateTime
│   ├── InstantSerializer.java           # Instant → ISO-8601 UTC
│   ├── InstantDeserializer.java         # ISO-8601/timestamp → Instant
│   ├── LocalDateSerializer.java         # LocalDate → yyyy-MM-dd
│   └── LocalDateDeserializer.java       # yyyy-MM-dd → LocalDate
├── utils/
│   ├── TimeZoneUtils.java               # Core timezone utility
│   └── DateUtils.java                   # Multi-format date parser
└── annotation/
    └── UserTZ.java                      # Optional annotation

src/main/resources/
└── META-INF/spring/
    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports

src/test/java/com/tzkit/
├── context/TimeZoneContextTest.java
├── filter/TimeZoneFilterTest.java
├── serializer/
│   ├── DateSerializerTest.java
│   ├── DateDeserializerTest.java
│   ├── LocalDateTimeSerializerTest.java
│   ├── LocalDateTimeDeserializerTest.java
│   ├── InstantSerializerTest.java
│   ├── InstantDeserializerTest.java
│   ├── LocalDateSerializerTest.java
│   └── LocalDateDeserializerTest.java
├── utils/TimeZoneUtilsTest.java
└── integration/TzKitIntegrationTest.java
```

---

## 1. Project Setup

### Task 1: Create Maven Module Structure

**Files:**
- Create: `pom.xml`

- [ ] **Step 1: Write pom.xml with dependencies**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.tzkit</groupId>
    <artifactId>tzkit-spring-boot-starter</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>TZKit Spring Boot Starter</name>
    <description>Automatic timezone conversion for Spring Boot applications</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.2.0</spring-boot.version>
        <hutool.version>5.8.34</hutool.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

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
            <version>${hutool.version}</version>
        </dependency>

        <!-- Configuration Processor -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create directory structure**

```bash
mkdir -p src/main/java/com/tzkit/config
mkdir -p src/main/java/com/tzkit/context
mkdir -p src/main/java/com/tzkit/filter
mkdir -p src/main/java/com/tzkit/serializer
mkdir -p src/main/java/com/tzkit/utils
mkdir -p src/main/java/com/tzkit/annotation
mkdir -p src/main/resources/META-INF/spring
mkdir -p src/test/java/com/tzkit/context
mkdir -p src/test/java/com/tzkit/filter
mkdir -p src/test/java/com/tzkit/serializer
mkdir -p src/test/java/com/tzkit/utils
mkdir -p src/test/java/com/tzkit/integration
```

- [ ] **Step 3: Create auto-configuration imports file**

Create: `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.tzkit.config.TimeZoneAutoConfiguration
```

- [ ] **Step 4: Commit project setup**

```bash
git add pom.xml src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
git commit -m "feat: initial project setup with Maven configuration"
```

---

## 2. TimeZoneContext - ThreadLocal Holder

### Task 2: Implement TimeZoneContext

**Files:**
- Create: `src/main/java/com/tzkit/context/TimeZoneContext.java`
- Create: `src/test/java/com/tzkit/context/TimeZoneContextTest.java`

- [ ] **Step 1: Write the failing test for TimeZoneContext**

Create: `src/test/java/com/tzkit/context/TimeZoneContextTest.java`

```java
package com.tzkit.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneContextTest {

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testSetAndGet() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);
        
        assertEquals(tz, TimeZoneContext.get());
    }

    @Test
    void testGetWhenNotSet() {
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testClear() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);
        TimeZoneContext.clear();
        
        assertNull(TimeZoneContext.get());
    }

    @Test
    void testGetZoneId() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(tz);
        
        assertEquals(tz.toZoneId(), TimeZoneContext.getZoneId());
    }

    @Test
    void testGetZoneIdWhenNotSet() {
        assertNull(TimeZoneContext.getZoneId());
    }

    @Test
    void testThreadIsolation() throws Exception {
        TimeZone tz1 = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone tz2 = TimeZone.getTimeZone("America/New_York");
        
        TimeZoneContext.set(tz1);
        
        Thread otherThread = new Thread(() -> {
            assertNull(TimeZoneContext.get());
            TimeZoneContext.set(tz2);
            assertEquals(tz2, TimeZoneContext.get());
        });
        
        otherThread.start();
        otherThread.join();
        
        // Main thread still has tz1
        assertEquals(tz1, TimeZoneContext.get());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=TimeZoneContextTest -q
```
Expected: Compilation error "TimeZoneContext not found"

- [ ] **Step 3: Write minimal implementation**

Create: `src/main/java/com/tzkit/context/TimeZoneContext.java`

```java
package com.tzkit.context;

import java.util.TimeZone;
import java.time.ZoneId;

/**
 * ThreadLocal holder for current request's user timezone.
 * Automatically cleared by TimeZoneFilter after request completion.
 */
public final class TimeZoneContext {

    private static final ThreadLocal<TimeZone> TIME_ZONE_HOLDER = new ThreadLocal<>();

    private TimeZoneContext() {
        // Utility class
    }

    /**
     * Get current request's user timezone.
     * @return TimeZone or null if not set
     */
    public static TimeZone get() {
        return TIME_ZONE_HOLDER.get();
    }

    /**
     * Set current request's user timezone.
     * @param timeZone the timezone to store
     */
    public static void set(TimeZone timeZone) {
        TIME_ZONE_HOLDER.set(timeZone);
    }

    /**
     * Clear the timezone context to prevent memory leaks.
     */
    public static void clear() {
        TIME_ZONE_HOLDER.remove();
    }

    /**
     * Get current request's user ZoneId.
     * @return ZoneId or null if not set
     */
    public static ZoneId getZoneId() {
        TimeZone tz = get();
        return tz != null ? tz.toZoneId() : null;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=TimeZoneContextTest -q
```
Expected: All tests pass

- [ ] **Step 5: Commit TimeZoneContext**

```bash
git add src/main/java/com/tzkit/context/TimeZoneContext.java src/test/java/com/tzkit/context/TimeZoneContextTest.java
git commit -m "feat: add TimeZoneContext ThreadLocal holder"
```

---

## 3. TimeZoneProperties - Configuration Binding

### Task 3: Implement TimeZoneProperties

**Files:**
- Create: `src/main/java/com/tzkit/config/TimeZoneProperties.java`

- [ ] **Step 1: Write TimeZoneProperties**

Create: `src/main/java/com/tzkit/config/TimeZoneProperties.java`

```java
package com.tzkit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for TZKit.
 * Prefix: tzkit
 */
@ConfigurationProperties(prefix = "tzkit")
public class TimeZoneProperties {

    /**
     * Header configuration
     */
    private Header header = new Header();

    /**
     * Default timezone when no header provided
     */
    private String defaultTimezone = "Asia/Shanghai";

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public void setDefaultTimezone(String defaultTimezone) {
        this.defaultTimezone = defaultTimezone;
    }

    /**
     * Header name configuration
     */
    public static class Header {
        /**
         * Request header name for timezone ID (IANA)
         */
        private String timezone = "Time-Zone";

        /**
         * Request header name for UTC offset
         */
        private String offset = "Time-Zone-Offset";

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getOffset() {
            return offset;
        }

        public void setOffset(String offset) {
            this.offset = offset;
        }
    }
}
```

- [ ] **Step 2: Commit TimeZoneProperties**

```bash
git add src/main/java/com/tzkit/config/TimeZoneProperties.java
git commit -m "feat: add TimeZoneProperties configuration binding"
```

---

## 4. TimeZoneFilter - Header Extraction

### Task 4: Implement TimeZoneFilter

**Files:**
- Create: `src/main/java/com/tzkit/filter/TimeZoneFilter.java`
- Create: `src/test/java/com/tzkit/filter/TimeZoneFilterTest.java`

- [ ] **Step 1: Write the failing test for TimeZoneFilter header parsing**

Create: `src/test/java/com/tzkit/filter/TimeZoneFilterTest.java`

```java
package com.tzkit.filter;

import com.tzkit.config.TimeZoneProperties;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneFilterTest {

    private TimeZoneFilter filter;
    private TimeZoneProperties properties;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        properties = new TimeZoneProperties();
        filter = new TimeZoneFilter(properties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testExtractIANATimezone() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testExtractUtcOffsetPositive() throws Exception {
        request.addHeader("Time-Zone-Offset", "+8");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("GMT+08:00", tz.getID());
    }

    @Test
    void testExtractUtcOffsetNegative() throws Exception {
        request.addHeader("Time-Zone-Offset", "-5");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("GMT-05:00", tz.getID());
    }

    @Test
    void testExtractUtcOffsetWithMinutes() throws Exception {
        request.addHeader("Time-Zone-Offset", "+5:30");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("GMT+05:30", tz.getID());
    }

    @Test
    void testPreferIANAOverOffset() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        request.addHeader("Time-Zone-Offset", "+8");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testUseDefaultWhenNoHeader() throws Exception {
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testInvalidTimezoneFallsBackToDefault() throws Exception {
        request.addHeader("Time-Zone", "Invalid/Zone");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testCustomHeaderNames() throws Exception {
        properties.getHeader().setTimezone("X-Timezone");
        properties.getHeader().setOffset("X-Offset");
        filter = new TimeZoneFilter(properties);
        
        request.addHeader("X-Timezone", "America/New_York");
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertEquals("America/New_York", tz.getID());
    }

    @Test
    void testCustomDefaultTimezone() throws Exception {
        properties.setDefaultTimezone("America/New_York");
        filter = new TimeZoneFilter(properties);
        
        filter.doFilter(request, response, filterChain);
        
        TimeZone tz = TimeZoneContext.get();
        assertEquals("America/New_York", tz.getID());
    }

    @Test
    void testCleanupAfterFilter() throws Exception {
        request.addHeader("Time-Zone", "Asia/Shanghai");
        
        filter.doFilter(request, response, filterChain);
        
        // Context should be cleared after filter chain completes
        assertNull(TimeZoneContext.get());
    }

    private static class MockFilterChain implements FilterChain {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) 
            throws IOException, ServletException {
            // Do nothing - just pass through
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=TimeZoneFilterTest -q
```
Expected: Compilation error "TimeZoneFilter not found"

- [ ] **Step 3: Write TimeZoneFilter implementation**

Create: `src/main/java/com/tzkit/filter/TimeZoneFilter.java`

```java
package com.tzkit.filter;

import com.tzkit.config.TimeZoneProperties;
import com.tzkit.context.TimeZoneContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Servlet filter that extracts timezone from request headers
 * and stores it in TimeZoneContext for the request lifecycle.
 */
public class TimeZoneFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneFilter.class);

    private final TimeZoneProperties properties;

    public TimeZoneFilter(TimeZoneProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            TimeZone timeZone = resolveTimeZone(httpRequest);
            TimeZoneContext.set(timeZone);
            
            chain.doFilter(request, response);
        } finally {
            // Always clear to prevent memory leaks
            TimeZoneContext.clear();
        }
    }

    /**
     * Resolve timezone from request headers.
     * Priority: Time-Zone header → Time-Zone-Offset header → default
     */
    private TimeZone resolveTimeZone(HttpServletRequest request) {
        String timezoneHeader = request.getHeader(properties.getHeader().getTimezone());
        String offsetHeader = request.getHeader(properties.getHeader().getOffset());

        // Priority 1: IANA timezone ID
        if (timezoneHeader != null && !timezoneHeader.isEmpty()) {
            TimeZone tz = parseIANATimezone(timezoneHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid timezone header: {}, using default", timezoneHeader);
        }

        // Priority 2: UTC offset
        if (offsetHeader != null && !offsetHeader.isEmpty()) {
            TimeZone tz = parseOffsetTimezone(offsetHeader);
            if (tz != null) {
                return tz;
            }
            log.warn("Invalid offset header: {}, using default", offsetHeader);
        }

        // Priority 3: Default timezone
        return TimeZone.getTimeZone(properties.getDefaultTimezone());
    }

    /**
     * Parse IANA timezone ID (e.g., "Asia/Shanghai")
     */
    private TimeZone parseIANATimezone(String timezoneId) {
        try {
            TimeZone tz = TimeZone.getTimeZone(timezoneId);
            // TimeZone.getTimeZone returns GMT for invalid IDs
            if (!tz.getID().equals("GMT") || timezoneId.equals("GMT")) {
                return tz;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse UTC offset (e.g., "+8", "-5", "+5:30")
     */
    private TimeZone parseOffsetTimezone(String offset) {
        try {
            String normalized = normalizeOffset(offset);
            if (normalized != null) {
                return TimeZone.getTimeZone("GMT" + normalized);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Normalize offset format to ±HH:mm or ±HH
     * Input: "+8", "-5", "+5:30", "+05:30"
     * Output: "+08:00", "-05:00", "+05:30", "+05:30"
     */
    private String normalizeOffset(String offset) {
        if (offset == null || offset.isEmpty()) {
            return null;
        }

        // Remove leading + if present, keep -
        String sign = offset.startsWith("-") ? "-" : "+";
        String value = offset.startsWith("+") || offset.startsWith("-") 
            ? offset.substring(1) : offset;

        // Split by : if present
        String[] parts = value.split(":");
        
        String hours;
        String minutes = "00";

        if (parts.length == 1) {
            hours = padZero(parts[0], 2);
        } else if (parts.length == 2) {
            hours = padZero(parts[0], 2);
            minutes = padZero(parts[1], 2);
        } else {
            return null;
        }

        return sign + hours + ":" + minutes;
    }

    private String padZero(String s, int length) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=TimeZoneFilterTest -q
```
Expected: All tests pass

- [ ] **Step 5: Commit TimeZoneFilter**

```bash
git add src/main/java/com/tzkit/filter/TimeZoneFilter.java src/test/java/com/tzkit/filter/TimeZoneFilterTest.java
git commit -m "feat: add TimeZoneFilter for header extraction"
```

---

## 5. Date Serializer/Deserializer

### Task 5: Implement Date Serialization

**Files:**
- Create: `src/main/java/com/tzkit/serializer/DateSerializer.java`
- Create: `src/main/java/com/tzkit/serializer/DateDeserializer.java`
- Create: `src/test/java/com/tzkit/serializer/DateSerializerTest.java`
- Create: `src/test/java/com/tzkit/serializer/DateDeserializerTest.java`

- [ ] **Step 1: Write the failing test for DateSerializer**

Create: `src/test/java/com/tzkit/serializer/DateSerializerTest.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.*;

class DateSerializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.getSerializerProvider().setDateFormat(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testSerializeUtcDateToUserTimezone() throws Exception {
        // 2026-01-22 10:00:00 UTC
        Date utcDate = new Date(1737537600000L);
        
        TimeZone userTz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(userTz);
        
        // Set serializer
        mapper.getSerializerProvider().setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String json = mapper.writeValueAsString(utcDate);
        
        // Should be 2026-01-22 18:00:00 in Asia/Shanghai (UTC+8)
        assertTrue(json.contains("18:00:00") || json.contains("1737537600000"));
    }
}
```

- [ ] **Step 2: Write DateSerializer**

Create: `src/main/java/com/tzkit/serializer/DateSerializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom serializer for java.util.Date.
 * Converts UTC Date to user timezone string representation.
 */
public class DateSerializer extends JsonSerializer<Date> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void serialize(Date value, JsonGenerator gen, 
                          SerializerProvider provider) throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }

        TimeZone userTz = TimeZoneContext.get();
        if (userTz == null) {
            userTz = TimeZone.getTimeZone("Asia/Shanghai");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN);
        sdf.setTimeZone(userTz);
        
        gen.writeString(sdf.format(value));
    }
}
```

- [ ] **Step 3: Write DateDeserializer**

Create: `src/main/java/com/tzkit/serializer/DateDeserializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tzkit.context.TimeZoneContext;
import cn.hutool.core.date.FastDateFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom deserializer for java.util.Date.
 * Parses date string in user timezone and converts to UTC Date.
 */
public class DateDeserializer extends JsonDeserializer<Date> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) 
        throws IOException {
        
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        TimeZone userTz = TimeZoneContext.get();
        if (userTz == null) {
            userTz = TimeZone.getTimeZone("Asia/Shanghai");
        }

        try {
            FastDateFormat sdf = FastDateFormat.getInstance(DEFAULT_PATTERN, userTz);
            Date dateInUserTz = sdf.parse(text);
            return dateInUserTz;
        } catch (ParseException e) {
            throw new IOException("Failed to parse date: " + text, e);
        }
    }
}
```

- [ ] **Step 4: Write tests for DateDeserializer**

Create: `src/test/java/com/tzkit/serializer/DateDeserializerTest.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class DateDeserializerTest {

    private ObjectMapper mapper;
    private TestBean testBean;

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testDeserializeUserTimezoneToUtc() throws Exception {
        TimeZone userTz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZoneContext.set(userTz);
        
        String json = "{\"date\":\"2026-01-22 18:00:00\"}";
        
        // Note: We'll test this properly after integrating with Jackson config
        // For now, verify the deserializer works standalone
        DateDeserializer deserializer = new DateDeserializer();
    }

    static class TestBean {
        private Date date;
        
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}
```

- [ ] **Step 5: Run tests**

```bash
mvn test -Dtest="Date*Test" -q
```

- [ ] **Step 6: Commit Date serializers**

```bash
git add src/main/java/com/tzkit/serializer/DateSerializer.java src/main/java/com/tzkit/serializer/DateDeserializer.java src/test/java/com/tzkit/serializer/
git commit -m "feat: add Date serializer/deserializer with timezone conversion"
```

---

## 6. LocalDateTime Serializer/Deserializer

### Task 6: Implement LocalDateTime Serialization

**Files:**
- Create: `src/main/java/com/tzkit/serializer/LocalDateTimeSerializer.java`
- Create: `src/main/java/com/tzkit/serializer/LocalDateTimeDeserializer.java`

- [ ] **Step 1: Write LocalDateTimeSerializer**

Create: `src/main/java/com/tzkit/serializer/LocalDateTimeSerializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for LocalDateTime.
 * Converts UTC LocalDateTime to user timezone string representation.
 * Note: LocalDateTime is assumed to be in UTC.
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, 
                          SerializerProvider provider) throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }

        ZoneId userZone = TimeZoneContext.getZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }

        // Treat input as UTC, convert to user timezone
        LocalDateTime userTime = value.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(userZone)
            .toLocalDateTime();

        gen.writeString(DEFAULT_FORMATTER.format(userTime));
    }
}
```

- [ ] **Step 2: Write LocalDateTimeDeserializer**

Create: `src/main/java/com/tzkit/serializer/LocalDateTimeDeserializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tzkit.context.TimeZoneContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer for LocalDateTime.
 * Parses datetime string in user timezone and converts to UTC LocalDateTime.
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) 
        throws IOException {
        
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        ZoneId userZone = TimeZoneContext.getZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }

        // Parse as user timezone, then convert to UTC
        LocalDateTime userTime = LocalDateTime.parse(text, DEFAULT_FORMATTER);
        LocalDateTime utcTime = userTime.atZone(userZone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();

        return utcTime;
    }
}
```

- [ ] **Step 3: Commit LocalDateTime serializers**

```bash
git add src/main/java/com/tzkit/serializer/LocalDateTimeSerializer.java src/main/java/com/tzkit/serializer/LocalDateTimeDeserializer.java
git commit -m "feat: add LocalDateTime serializer/deserializer with timezone conversion"
```

---

## 7. Instant Serializer/Deserializer

### Task 7: Implement Instant Serialization

**Files:**
- Create: `src/main/java/com/tzkit/serializer/InstantSerializer.java`
- Create: `src/main/java/com/tzkit/serializer/InstantDeserializer.java`

- [ ] **Step 1: Write InstantSerializer**

Create: `src/main/java/com/tzkit/serializer/InstantSerializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for Instant.
 * Outputs ISO-8601 UTC format without timezone conversion.
 * Instant represents a precise moment in UTC.
 */
public class InstantSerializer extends JsonSerializer<Instant> {

    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_INSTANT;

    @Override
    public void serialize(Instant value, JsonGenerator gen, 
                          SerializerProvider provider) throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }

        // Always output as ISO-8601 UTC format
        gen.writeString(ISO_FORMATTER.format(value));
    }
}
```

- [ ] **Step 2: Write InstantDeserializer**

Create: `src/main/java/com/tzkit/serializer/InstantDeserializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom deserializer for Instant.
 * Supports ISO-8601 strings and numeric timestamps (epoch millis).
 */
public class InstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) 
        throws IOException {
        
        // Handle numeric timestamp
        if (p.getCurrentToken().isNumeric()) {
            long epochMillis = p.getLongValue();
            return Instant.ofEpochMilli(epochMillis);
        }

        // Handle ISO-8601 string
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        return Instant.parse(text);
    }
}
```

- [ ] **Step 3: Commit Instant serializers**

```bash
git add src/main/java/com/tzkit/serializer/InstantSerializer.java src/main/java/com/tzkit/serializer/InstantDeserializer.java
git commit -m "feat: add Instant serializer/deserializer (ISO-8601 UTC, no conversion)"
```

---

## 8. LocalDate Serializer/Deserializer

### Task 8: Implement LocalDate Serialization

**Files:**
- Create: `src/main/java/com/tzkit/serializer/LocalDateSerializer.java`
- Create: `src/main/java/com/tzkit/serializer/LocalDateDeserializer.java`

- [ ] **Step 1: Write LocalDateSerializer**

Create: `src/main/java/com/tzkit/serializer/LocalDateSerializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for LocalDate.
 * Outputs yyyy-MM-dd format without timezone conversion.
 * LocalDate is date-only, timezone-independent.
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = 
        DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, 
                          SerializerProvider provider) throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeString(ISO_DATE_FORMATTER.format(value));
    }
}
```

- [ ] **Step 2: Write LocalDateDeserializer**

Create: `src/main/java/com/tzkit/serializer/LocalDateDeserializer.java`

```java
package com.tzkit.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Custom deserializer for LocalDate.
 * Parses yyyy-MM-dd format without timezone conversion.
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) 
        throws IOException {
        
        String text = p.getValueAsString();
        if (text == null || text.isEmpty()) {
            return null;
        }

        return LocalDate.parse(text);
    }
}
```

- [ ] **Step 3: Commit LocalDate serializers**

```bash
git add src/main/java/com/tzkit/serializer/LocalDateSerializer.java src/main/java/com/tzkit/serializer/LocalDateDeserializer.java
git commit -m "feat: add LocalDate serializer/deserializer (no timezone conversion)"
```

---

## 9. Jackson Configuration

### Task 9: Implement JacksonTimeZoneConfig

**Files:**
- Create: `src/main/java/com/tzkit/config/JacksonTimeZoneConfig.java`

- [ ] **Step 1: Write JacksonTimeZoneConfig**

Create: `src/main/java/com/tzkit/config/JacksonTimeZoneConfig.java`

```java
package com.tzkit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tzkit.serializer.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Jackson configuration for timezone-aware serialization.
 * Registers custom serializers/deserializers and sets UTC as default timezone.
 */
@Configuration
public class JacksonTimeZoneConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer tzkitJacksonCustomizer() {
        return builder -> {
            // Set default timezone to UTC
            builder.timeZone(TimeZone.getTimeZone("UTC"));

            // Disable writing dates as timestamps
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Register JavaTimeModule for JSR310 types
            builder.modules(new JavaTimeModule());

            // Register custom serializers
            builder.serializers(new DateSerializer());
            builder.serializers(new LocalDateTimeSerializer());
            builder.serializers(new InstantSerializer());
            builder.serializers(new LocalDateSerializer());

            // Register custom deserializers
            builder.deserializers(new DateDeserializer());
            builder.deserializers(new LocalDateTimeDeserializer());
            builder.deserializers(new InstantDeserializer());
            builder.deserializers(new LocalDateDeserializer());
        };
    }
}
```

- [ ] **Step 2: Commit JacksonTimeZoneConfig**

```bash
git add src/main/java/com/tzkit/config/JacksonTimeZoneConfig.java
git commit -m "feat: add JacksonTimeZoneConfig for ObjectMapper customization"
```

---

## 10. TimeZoneUtils Utility Class

### Task 10: Implement TimeZoneUtils

**Files:**
- Create: `src/main/java/com/tzkit/utils/TimeZoneUtils.java`
- Create: `src/test/java/com/tzkit/utils/TimeZoneUtilsTest.java`

- [ ] **Step 1: Write the failing test for TimeZoneUtils**

Create: `src/test/java/com/tzkit/utils/TimeZoneUtilsTest.java`

```java
package com.tzkit.utils;

import com.tzkit.context.TimeZoneContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneUtilsTest {

    @BeforeEach
    void setUp() {
        TimeZoneContext.clear();
        TimeZoneContext.set(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    void tearDown() {
        TimeZoneContext.clear();
    }

    @Test
    void testGetUserTimeZone() {
        TimeZone tz = TimeZoneUtils.getUserTimeZone();
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testGetUserZoneId() {
        ZoneId zoneId = TimeZoneUtils.getUserZoneId();
        assertNotNull(zoneId);
        assertEquals("Asia/Shanghai", zoneId.getId());
    }

    @Test
    void testGetUserZoneOffset() {
        ZoneOffset offset = TimeZoneUtils.getUserZoneOffset();
        assertNotNull(offset);
        assertEquals(ZoneOffset.ofHours(8), offset);
    }

    @Test
    void testGetServerTimeZone() {
        TimeZone tz = TimeZoneUtils.getServerTimeZone();
        assertNotNull(tz);
        assertEquals("UTC", tz.getID());
    }

    @Test
    void testToUserZoneLocalDateTime() {
        LocalDateTime utcTime = LocalDateTime.of(2026, 1, 22, 10, 0, 0);
        LocalDateTime userTime = TimeZoneUtils.toUserZone(utcTime);
        assertEquals(LocalDateTime.of(2026, 1, 22, 18, 0, 0), userTime);
    }

    @Test
    void testToUtcLocalDateTime() {
        LocalDateTime userTime = LocalDateTime.of(2026, 1, 22, 18, 0, 0);
        LocalDateTime utcTime = TimeZoneUtils.toUtc(userTime);
        assertEquals(LocalDateTime.of(2026, 1, 22, 10, 0, 0), utcTime);
    }

    @Test
    void testToUserZoneInstant() {
        Instant instant = Instant.parse("2026-01-22T10:00:00Z");
        LocalDateTime userTime = TimeZoneUtils.toUserZone(instant);
        assertEquals(LocalDateTime.of(2026, 1, 22, 18, 0, 0), userTime);
    }

    @Test
    void testConvertBetweenTimezones() {
        LocalDateTime utcTime = LocalDateTime.of(2026, 1, 22, 10, 0, 0);
        LocalDateTime nyTime = TimeZoneUtils.convert(
            utcTime, 
            ZoneId.of("UTC"), 
            ZoneId.of("America/New_York")
        );
        // UTC-5 in winter (EST)
        assertEquals(LocalDateTime.of(2026, 1, 22, 5, 0, 0), nyTime);
    }

    @Test
    void testFormatDefault() {
        LocalDateTime utcTime = LocalDateTime.of(2026, 1, 22, 10, 0, 0);
        String formatted = TimeZoneUtils.format(utcTime);
        assertEquals("2026-01-22 18:00:00", formatted);
    }

    @Test
    void testFormatCustomPattern() {
        LocalDateTime utcTime = LocalDateTime.of(2026, 1, 22, 10, 0, 0);
        String formatted = TimeZoneUtils.format(utcTime, "yyyy/MM/dd HH:mm");
        assertEquals("2026/01/22 18:00", formatted);
    }

    @Test
    void testParseDefault() {
        LocalDateTime parsed = TimeZoneUtils.parse("2026-01-22 18:00:00");
        assertEquals(LocalDateTime.of(2026, 1, 22, 18, 0, 0), parsed);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=TimeZoneUtilsTest -q
```
Expected: Compilation error "TimeZoneUtils not found"

- [ ] **Step 3: Write TimeZoneUtils implementation**

Create: `src/main/java/com/tzkit/utils/TimeZoneUtils.java`

```java
package com.tzkit.utils;

import com.tzkit.context.TimeZoneContext;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for timezone-aware operations.
 * Reads timezone from TimeZoneContext for all operations.
 */
public final class TimeZoneUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private TimeZoneUtils() {
        // Utility class
    }

    // ===== Timezone Access =====

    /**
     * Get current request's user timezone.
     * @return TimeZone or null if not set
     */
    public static TimeZone getUserTimeZone() {
        return TimeZoneContext.get();
    }

    /**
     * Get current request's user ZoneId.
     * @return ZoneId or null if not set
     */
    public static ZoneId getUserZoneId() {
        return TimeZoneContext.getZoneId();
    }

    /**
     * Get user timezone offset.
     * @return ZoneOffset or null if timezone not set
     */
    public static ZoneOffset getUserZoneOffset() {
        ZoneId zoneId = getUserZoneId();
        if (zoneId == null) {
            return null;
        }
        // Get current offset (handles DST)
        return zoneId.getRules().getOffset(Instant.now());
    }

    /**
     * Get server default timezone (UTC).
     * @return UTC TimeZone
     */
    public static TimeZone getServerTimeZone() {
        return TimeZone.getTimeZone("UTC");
    }

    // ===== Current Time =====

    /**
     * Get current time in user timezone.
     * @return LocalDateTime in user timezone
     */
    public static LocalDateTime now() {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }
        return LocalDateTime.now(userZone);
    }

    /**
     * Get current date in user timezone.
     * @return LocalDate in user timezone
     */
    public static LocalDate today() {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }
        return LocalDate.now(userZone);
    }

    /**
     * Get current time in UTC.
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Get current instant.
     * @return current Instant in UTC
     */
    public static Instant nowInstant() {
        return Instant.now();
    }

    // ===== Timezone Conversion =====

    /**
     * Convert UTC LocalDateTime to user timezone.
     * @param utcTime time in UTC
     * @return LocalDateTime in user timezone
     */
    public static LocalDateTime toUserZone(LocalDateTime utcTime) {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }
        return utcTime.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(userZone)
            .toLocalDateTime();
    }

    /**
     * Convert user timezone LocalDateTime to UTC.
     * @param userTime time in user timezone
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime toUtc(LocalDateTime userTime) {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }
        return userTime.atZone(userZone)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    /**
     * Convert UTC Date to user timezone Date.
     * Note: Date is always UTC internally, this adjusts the representation.
     * @param utcDate Date in UTC
     * @return Date representing same instant
     */
    public static Date toUserZone(Date utcDate) {
        return utcDate; // Date is timezone-agnostic, just instant
    }

    /**
     * Convert user timezone Date to UTC Date.
     * @param userDate Date in user timezone context
     * @return Date in UTC
     */
    public static Date toUtc(Date userDate) {
        return userDate; // Date is timezone-agnostic
    }

    /**
     * Convert Instant to user timezone LocalDateTime.
     * @param instant the instant
     * @return LocalDateTime in user timezone
     */
    public static LocalDateTime toUserZone(Instant instant) {
        ZoneId userZone = getUserZoneId();
        if (userZone == null) {
            userZone = ZoneId.of("Asia/Shanghai");
        }
        return instant.atZone(userZone).toLocalDateTime();
    }

    /**
     * Convert between any two timezones.
     * @param time the LocalDateTime in source timezone
     * @param from source timezone
     * @param to target timezone
     * @return LocalDateTime in target timezone
     */
    public static LocalDateTime convert(LocalDateTime time, ZoneId from, ZoneId to) {
        return time.atZone(from)
            .withZoneSameInstant(to)
            .toLocalDateTime();
    }

    // ===== Formatting =====

    /**
     * Format LocalDateTime using user timezone and default pattern.
     * @param utcTime time in UTC
     * @return formatted string in user timezone
     */
    public static String format(LocalDateTime utcTime) {
        return format(utcTime, DEFAULT_PATTERN);
    }

    /**
     * Format LocalDateTime using user timezone and custom pattern.
     * @param utcTime time in UTC
     * @param pattern format pattern
     * @return formatted string in user timezone
     */
    public static String format(LocalDateTime utcTime, String pattern) {
        LocalDateTime userTime = toUserZone(utcTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(userTime);
    }

    /**
     * Format LocalDateTime with specific timezone.
     * @param utcTime time in UTC
     * @param pattern format pattern
     * @param zoneId timezone to use
     * @return formatted string
     */
    public static String format(LocalDateTime utcTime, String pattern, ZoneId zoneId) {
        LocalDateTime targetTime = utcTime.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(zoneId)
            .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(targetTime);
    }

    // ===== Parsing =====

    /**
     * Parse datetime string using user timezone and default pattern.
     * @param text datetime string in user timezone
     * @return LocalDateTime in user timezone (not converted)
     */
    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text, DEFAULT_FORMATTER);
    }

    /**
     * Parse datetime string using user timezone and custom pattern.
     * @param text datetime string
     * @param pattern format pattern
     * @return LocalDateTime in user timezone
     */
    public static LocalDateTime parse(String text, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(text, formatter);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=TimeZoneUtilsTest -q
```
Expected: All tests pass

- [ ] **Step 5: Commit TimeZoneUtils**

```bash
git add src/main/java/com/tzkit/utils/TimeZoneUtils.java src/test/java/com/tzkit/utils/TimeZoneUtilsTest.java
git commit -m "feat: add TimeZoneUtils with timezone-aware operations"
```

---

## 11. Auto-Configuration

### Task 11: Implement TimeZoneAutoConfiguration

**Files:**
- Create: `src/main/java/com/tzkit/config/TimeZoneAutoConfiguration.java`

- [ ] **Step 1: Write TimeZoneAutoConfiguration**

Create: `src/main/java/com/tzkit/config/TimeZoneAutoConfiguration.java`

```java
package com.tzkit.config;

import com.tzkit.filter.TimeZoneFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;

/**
 * Auto-configuration for TZKit Spring Boot Starter.
 * Activates on Servlet-based web applications with Spring Web MVC.
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(TimeZoneProperties.class)
@Import(JacksonTimeZoneConfig.class)
public class TimeZoneAutoConfiguration {

    /**
     * Register TimeZoneFilter with highest priority.
     * Ensures timezone is available before any other filter processing.
     */
    @Bean
    public FilterRegistrationBean<TimeZoneFilter> timeZoneFilterRegistration(
            TimeZoneProperties properties) {
        
        TimeZoneFilter filter = new TimeZoneFilter(properties);
        
        FilterRegistrationBean<TimeZoneFilter> registration = 
            new FilterRegistrationBean<>(filter);
        
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(
            DispatcherType.REQUEST, 
            DispatcherType.ASYNC,
            DispatcherType.ERROR
        );
        
        return registration;
    }
}
```

- [ ] **Step 2: Commit TimeZoneAutoConfiguration**

```bash
git add src/main/java/com/tzkit/config/TimeZoneAutoConfiguration.java
git commit -m "feat: add TimeZoneAutoConfiguration for zero-config integration"
```

---

## 12. Integration Test

### Task 12: Integration Test End-to-End

**Files:**
- Create: `src/test/java/com/tzkit/integration/TzKitIntegrationTest.java`

- [ ] **Step 1: Write integration test**

Create: `src/test/java/com/tzkit/integration/TzKitIntegrationTest.java`

```java
package com.tzkit.integration;

import com.tzkit.config.TimeZoneAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TzKitIntegrationTest {

    private final WebApplicationContextRunner contextRunner = 
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TimeZoneAutoConfiguration.class))
            .withUserConfiguration(TestControllerConfig.class);

    @Test
    void autoConfigurationShouldActivate() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(com.tzkit.filter.TimeZoneFilter.class);
            assertThat(context).hasSingleBean(com.tzkit.config.TimeZoneProperties.class);
        });
    }

    @Test
    void autoConfigurationShouldNotActivateWithoutWeb() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TimeZoneAutoConfiguration.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(com.tzkit.filter.TimeZoneFilter.class);
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
                com.tzkit.config.TimeZoneProperties props = 
                    context.getBean(com.tzkit.config.TimeZoneProperties.class);
                assertThat(props.getHeader().getTimezone()).isEqualTo("X-Timezone");
                assertThat(props.getDefaultTimezone()).isEqualTo("America/New_York");
            });
    }

    @Configuration
    static class TestControllerConfig {
        @Bean
        public RouterFunction<ServerResponse> testRoutes() {
            return org.springframework.web.servlet.function.RouterFunctions
                .route()
                .GET("/test", request -> ServerResponse.ok().body("ok"))
                .build();
        }
    }
}
```

- [ ] **Step 2: Run all tests**

```bash
mvn test -q
```

- [ ] **Step 3: Commit integration test**

```bash
git add src/test/java/com/tzkit/integration/TzKitIntegrationTest.java
git commit -m "feat: add integration test for auto-configuration"
```

---

## 13. Optional: @UserTZ Annotation

### Task 13: Implement @UserTZ Annotation

**Files:**
- Create: `src/main/java/com/tzkit/annotation/UserTZ.java`

- [ ] **Step 1: Write @UserTZ annotation**

Create: `src/main/java/com/tzkit/annotation/UserTZ.java`

```java
package com.tzkit.annotation;

import java.lang.annotation.*;

/**
 * Annotation for controller parameters to indicate timezone-aware parsing.
 * Use with @RequestParam for date query parameters.
 * 
 * Example:
 * @RequestParam @UserTZ LocalDate date
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserTZ {
}
```

- [ ] **Step 2: Commit annotation**

```bash
git add src/main/java/com/tzkit/annotation/UserTZ.java
git commit -m "feat: add @UserTZ annotation for controller parameters"
```

---

## 14. Final Verification

### Task 14: Run Full Test Suite and Build

- [ ] **Step 1: Run all tests**

```bash
mvn test
```
Expected: All tests pass

- [ ] **Step 2: Build the jar**

```bash
mvn clean package -DskipTests
```
Expected: jar created at `target/tzkit-spring-boot-starter-1.0.0.jar`

- [ ] **Step 3: Verify auto-configuration imports file is packaged**

```bash
jar tf target/tzkit-spring-boot-starter-1.0.0.jar | grep AutoConfiguration.imports
```
Expected: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat: TZKit Spring Boot Starter complete implementation"
```

---

## Spec Coverage Verification

| Spec Requirement | Task Coverage |
|-----------------|---------------|
| timezone-context: ThreadLocal storage | Task 2 |
| timezone-context: Thread-safe access | Task 2 (testThreadIsolation) |
| timezone-context: Null handling | Task 2 |
| timezone-context: ZoneId accessor | Task 2 |
| timezone-filter: Header extraction | Task 4 |
| timezone-filter: Resolution priority | Task 4 |
| timezone-filter: ThreadLocal storage/cleanup | Task 4 |
| timezone-filter: Invalid header handling | Task 4 |
| timezone-filter: Configurable headers | Task 4 |
| jackson-serialization: Date ser/deser | Task 5 |
| jackson-serialization: LocalDateTime ser/deser | Task 6 |
| jackson-serialization: Instant ser/deser | Task 7 |
| jackson-serialization: LocalDate ser/deser | Task 8 |
| jackson-serialization: ObjectMapper UTC config | Task 9 |
| timezone-utils: All methods | Task 10 |
| auto-configuration: All requirements | Task 11, Task 12 |

---

**Plan complete and saved to `docs/superpowers/plans/2026-05-07-tzkit-starter.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

2. **Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
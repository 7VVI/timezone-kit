# TZKit Spring Boot Starter

一个 Spring Boot 3.x 时区自动转换组件，透明处理 UTC 与用户时区之间的时间转换。

## 功能特性

- ✅ **自动时区转换** - JSON 序列化/反序列化时自动转换时间
- ✅ **多格式解析** - 支持多种日期格式自动识别解析
- ✅ **QueryParam 支持** - @RequestParam 参数自动转换
- ✅ **@JsonFormat 支持** - 字段级别格式覆盖
- ✅ **零配置集成** - Spring Boot 自动配置，开箱即用
- ✅ **线程安全** - ThreadLocal 管理请求级时区上下文

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tzkit</groupId>
    <artifactId>tzkit-spring-boot-starter</artifactId>
    <version>1.0.0-jdk11</version>
</dependency>
```

### 2. 前端传递时区

前端在请求头中传递用户时区：

```javascript
// 方式1: IANA 时区 ID
fetch('/api/users', {
    headers: { 'Time-Zone': 'Asia/Shanghai' }
});

// 方式2: UTC 偏移量
fetch('/api/users', {
    headers: { 'Time-Zone-Offset': '+8' }
});

// 方式3: 带分钟的偏移量
fetch('/api/users', {
    headers: { 'Time-Zone-Offset': '+5:30' }
});
```

### 3. 后端自动转换

```java
@RestController
public class UserController {

    @PostMapping("/users")
    public User createUser(@RequestBody UserDTO dto) {
        // 前端传入: "2026-01-22 18:00:00" (用户时区 Asia/Shanghai)
        // 后端接收: UTC 时间 2026-01-22 10:00:00
        return userService.create(dto);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        // 后端存储: UTC 时间 2026-01-22 10:00:00
        // 前端接收: "2026-01-22 18:00:00" (用户时区 Asia/Shanghai)
        return user;
    }
}
```

## 支持的日期格式

### Date / LocalDateTime 反序列化支持的格式

| 类型 | 格式示例 | 说明 |
|------|---------|------|
| 标准格式 | `2026-01-22 18:00:00` | 常用格式 |
| 标准格式 | `2026-01-22 18:00` | 精确到分钟 |
| 标准格式 | `2026-01-22` | 仅日期 |
| 斜杠分隔 | `2026/01/22 18:00:00` | 斜杠格式 |
| 斜杠分隔 | `2026/01/22` | 仅日期 |
| 纯数字 | `20260122180000` | 无分隔符 |
| 纯数字 | `20260122` | 仅日期 |
| 纯数字 | `180000` | 仅时间 |
| 时间戳 | `1737537600000` | 13位毫秒时间戳 |
| 中文格式 | `2026年01月22日 18时00分00秒` | 中文分隔符 |
| 中文格式 | `2026年01月22日` | 仅日期 |
| ISO格式 | `2026-01-22T18:00:00` | ISO 8601 |

### Instant 序列化/反序列化

Instant 始终使用 ISO-8601 UTC 格式，不进行时区转换：

```json
// 序列化输出
"2026-01-22T10:00:00Z"

// 反序列化支持
"2026-01-22T10:00:00Z"  // ISO字符串
1737537600000           // 时间戳
```

### LocalDate 序列化/反序列化

LocalDate 仅日期，无时区转换：

```json
// 序列化输出
"2026-01-22"

// 反序列化
"2026-01-22"
```

## 配置选项

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `tzkit.header.timezone` | `Time-Zone` | 时区ID请求头名称 |
| `tzkit.header.offset` | `Time-Zone-Offset` | UTC偏移量请求头名称 |
| `tzkit.default-timezone` | `Asia/Shanghai` | 默认用户时区（无请求头时使用） |
| `tzkit.server-timezone` | `UTC` | 后端服务器时区（数据存储/处理的基准时区） |

### 配置示例

```yaml
# application.yml
tzkit:
  header:
    timezone: X-Timezone      # 自定义时区头名称
    offset: X-Offset           # 自定义偏移量头名称
  default-timezone: America/New_York  # 自定义默认用户时区
  server-timezone: Asia/Shanghai       # 自定义服务器时区（默认UTC）
```

## @JsonFormat 注解支持

字段级别覆盖默认格式：

```java
public class Order {

    // 使用自定义格式和时区
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "America/New_York")
    private Date createTime;

    // 使用自定义格式（时区从请求头获取）
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updateTime;

    // Instant 不受 @JsonFormat 时区影响，始终 UTC
    private Instant timestamp;
}
```

## QueryParam 参数转换

支持 @RequestParam 时间参数自动解析：

```java
@RestController
public class ReportController {

    @GetMapping("/reports")
    public List<Report> getReports(
        @RequestParam Date startTime,      // 多格式自动解析
        @RequestParam Date endTime
    ) {
        // 支持: "2026-01-22", "20260122", "1737537600000" 等格式
        return reportService.findByTimeRange(startTime, endTime);
    }

    @GetMapping("/daily/{date}")
    public Report getDailyReport(
        @PathVariable @RequestParam LocalDate date
    ) {
        return reportService.getDailyReport(date);
    }

    @GetMapping("/logs")
    public List<Log> getLogs(
        @RequestParam LocalDateTime start
    ) {
        return logService.findByStartTime(start);
    }
}
```

## DateUtils 工具类

提供时区相关的便捷操作：

```java
import com.tzkit.utils.DateUtils;

// ===== 多格式解析 =====
DateUtils.parse("2026-01-22 18:00:00");    // 标准格式
DateUtils.parse("20260122180000");          // 纯数字
DateUtils.parse("1737537600000");           // 时间戳
DateUtils.parse("2026年01月22日");          // 中文格式
DateUtils.parseLdt("2026-01-22 18:00:00");  // LocalDateTime

// ===== 时区获取 =====
TimeZone tz = DateUtils.getTimeZone();      // 用户时区
ZoneId zone = DateUtils.getZoneId();        // 用户ZoneId
ZoneOffset offset = DateUtils.getZoneOffset();  // 用户时区偏移量
TimeZone serverTz = DateUtils.getServerTimeZone();  // 服务器时区
ZoneId serverZone = DateUtils.getServerZoneId();    // 服务器ZoneId

// ===== 当前时间 =====
LocalDateTime now = DateUtils.now();        // 用户时区当前时间
LocalDate today = DateUtils.today();        // 用户时区当前日期
LocalDateTime serverNow = DateUtils.nowServer(); // 服务器时区当前时间
LocalDateTime utcNow = DateUtils.nowUtc();  // UTC当前时间
Instant instant = DateUtils.nowInstant();   // 当前Instant

// ===== 时区转换 =====
LocalDateTime userTime = DateUtils.toUserZone(serverTime);  // 服务器时区 → 用户时区
LocalDateTime serverTime = DateUtils.toServerZone(userTime); // 用户时区 → 服务器时区
LocalDateTime converted = DateUtils.convert(time, fromZone, toZone);  // 任意转换

// ===== 格式化 =====
String str = DateUtils.format(serverTime);                     // 默认格式
String str = DateUtils.format(serverTime, "yyyy/MM/dd HH:mm"); // 自定义格式
String str = DateUtils.format(date);                        // Date格式化
```

## 工作原理

### 数据流程

```
前端请求
  │
  ├─ Header: Time-Zone: Asia/Shanghai
  │
  ▼
TimeZoneFilter (Servlet过滤器)
  │ 解析请求头 → 存储到 TimeZoneContext (ThreadLocal)
  │ 初始化服务器时区 → TimeZoneContextHolder (配置的 server-timezone)
  │
  ▼
Jackson反序列化 (请求体)
  │ Date/LocalDateTime反序列化器
  │ 读取用户时区: "2026-01-22 18:00:00" → 服务器时区 Date/LocalDateTime
  │
  ▼
Controller / Service
  │ 业务代码处理服务器时区时间
  │ 可使用 DateUtils 工具类
  │
  ▼
Jackson序列化 (响应体)
  │ Date/LocalDateTime序列化器
  │ 读取用户时区: 服务器时区 → "2026-01-22 18:00:00"
  │
  ▼
TimeZoneFilter.afterCompletion()
  │ 清理 ThreadLocal 防止内存泄漏
  │
  ▼
前端收到用户时区时间
```

### 时区解析优先级

1. **Time-Zone 头** → IANA 时区 ID (如 `Asia/Shanghai`)
2. **Time-Zone-Offset 头** → UTC 偏移量 (如 `+8`, `+5:30`)
3. **配置默认值** → `tzkit.default-timezone`

## 技术要求

- Java 11+
- Spring Boot 2.7.x
- Jackson (spring-boot-starter-json)
- Hutool 5.8.x

## 项目结构

```
src/main/java/com/tzkit/
├── annotation/
│   └── UserTZ.java                    # 用户时区注解(可选)
├── config/
│   ├── TimeZoneAutoConfiguration.java # 自动配置入口
│   ├── TimeZoneProperties.java        # 配置属性绑定
│   ├── JacksonTimeZoneConfig.java     # Jackson配置
│   └── WebMvcConfig.java              # Web MVC配置
├── context/
│   ├── TimeZoneContext.java           # ThreadLocal用户时区持有者
│   └── TimeZoneContextHolder.java     # 服务器时区持有者（静态全局配置）
├── converter/
│   ├── UserTimeZoneDateConverter.java      # Date参数转换器
│   ├── UserTimeZoneLocalDateConverter.java # LocalDate参数转换器
│   └── UserTimeZoneLocalDateTimeConverter.java # LocalDateTime参数转换器
├── filter/
│   └── TimeZoneFilter.java            # Servlet过滤器
├── serializer/
│   ├── DateSerializer.java            # Date序列化器
│   ├── DateDeserializer.java          # Date反序列化器
│   ├── LocalDateTimeSerializer.java   # LocalDateTime序列化器
│   ├── LocalDateTimeDeserializer.java # LocalDateTime反序列化器
│   ├── InstantSerializer.java         # Instant序列化器(ISO-8601)
│   ├── InstantDeserializer.java       # Instant反序列化器
│   ├── LocalDateSerializer.java       # LocalDate序列化器
│   └── LocalDateDeserializer.java     # LocalDate反序列化器
├── utils/
│   └── DateUtils.java                 # 时间工具类(多格式解析+时区转换)
```

## 注意事项

### 1. 异步任务

后台任务没有请求上下文，`TimeZoneContext.get()` 返回 null：

```java
@Async
public void processInBackground() {
    // TimeZoneContext.get() 返回 null
    // 使用 DateUtils.now() 会使用默认时区 Asia/Shanghai
    
    // 如果需要特定时区，手动设置
    TimeZoneContext.set(TimeZone.getTimeZone("America/New_York"));
    try {
        // 执行任务
    } finally {
        TimeZoneContext.clear();  // 必须清理
    }
}
```

### 2. Date 类型说明

`java.util.Date` 内部始终存储 UTC 时间戳，时区转换仅影响字符串表示：

```java
// DateUtils.toUserZone(Date) 和 toUtc(Date) 返回原对象
// 因为 Date 本身是时区无关的
Date date = DateUtils.parse("2026-01-22 18:00:00");
// date 内部: UTC时间戳 1737537600000

// 格式化时才会体现时区
String str = DateUtils.format(date);  // 用户时区字符串
```

### 3. Instant 和 LocalDate 说明

- **Instant**: 始终 UTC，序列化为 ISO-8601 格式，不受时区转换影响
- **LocalDate**: 仅日期，无时间信息，不受时区转换影响

## 版本历史

- **1.0.0-jdk11** - JDK11版本
  - 自动时区转换
  - 多格式日期解析
  - @JsonFormat 支持
  - QueryParam 参数转换
  - DateUtils 工具类
  - 可配置服务器时区（支持 tzkit.server-timezone 配置）

## 许可证

Apache License 2.0
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Test Commands

```bash
# 编译
mvn compile

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=TimeZoneContextTest
mvn test -Dtest=DateDeserializerTest

# 打包
mvn package -DskipTests

# 清理并构建
mvn clean package
```

## 架构概述

这是一个 Spring Boot 3.x Starter，用于自动时区转换。核心架构模式：**Filter + ThreadLocal + Jackson序列化器**。

### 数据流程

```
请求 → TimeZoneFilter → TimeZoneContext(ThreadLocal) → Jackson序列化器 → 响应
```

1. **TimeZoneFilter** (Servlet过滤器) 从请求头提取时区，存入 ThreadLocal
2. **TimeZoneContext** (ThreadLocal) 持有当前请求的用户时区
3. **Jackson序列化器** 从 ThreadLocal 读取时区，自动转换时间
4. **DateUtils** 工具类提供多格式解析和时区操作

### 核心组件职责

| 层 | 文件 | 职责 |
|---|------|------|
| Context | `TimeZoneContext.java` | ThreadLocal时区持有者，请求级存储 |
| Filter | `TimeZoneFilter.java` | 解析请求头，设置/清理ThreadLocal |
| Config | `TimeZoneAutoConfiguration.java` | 自动配置入口，注册Filter |
| Config | `JacksonTimeZoneConfig.java` | 注册自定义序列化器到Jackson |
| Serializer | `*Serializer.java` | UTC → 用户时区转换 |
| Deserializer | `*Deserializer.java` | 用户时区 → UTC转换，多格式解析 |
| Utils | `DateUtils.java` | 多格式解析、时区操作、格式化 |

### 时区解析优先级

```
Time-Zone头(IANA ID) → Time-Zone-Offset头(UTC偏移) → 默认值(Asia/Shanghai)
```

### 序列化器设计决策

| 类型 | 时区转换 | 说明 |
|------|---------|------|
| Date | ✅ 有 | UTC ↔ 用户时区 |
| LocalDateTime | ✅ 有 | UTC ↔ 用户时区 |
| Instant | ❌ 无 | 始终ISO-8601 UTC格式 |
| LocalDate | ❌ 无 | 仅日期，无时区信息 |

## 多格式日期解析

`DateUtils.parse()` 支持自动识别以下格式：

- 标准格式: `yyyy-MM-dd HH:mm:ss`, `yyyy-MM-dd`
- 纯数字: `yyyyMMddHHmmss`, 时间戳(13位)
- 斜杠分隔: `yyyy/MM/dd`
- 中文格式: `yyyy年MM月dd日 HH时mm分ss秒`

使用 Hutool 的 `FastDateFormat` 实现，需指定 TimeZone。

## @JsonFormat 支持

序列化器实现 `ContextualSerializer`/`ContextualDeserializer`，可读取字段上的 `@JsonFormat(pattern, timezone)` 注解进行字段级别覆盖。

## 代码风格

- 所有注释使用**中文**
- 使用 `Locale.ENGLISH` 在 SimpleDateFormat/DateTimeFormatter
- 默认时区硬编码为 `Asia/Shanghai`（可通过 `tzkit.default-timezone` 配置）
- ThreadLocal 清理使用 `try-finally` 模式，防止内存泄漏

## 配置属性

前缀 `tzkit`:
- `header.timezone`: 时区ID请求头名 (默认: `Time-Zone`)
- `header.offset`: UTC偏移请求头名 (默认: `Time-Zone-Offset`)
- `default-timezone`: 默认时区 (默认: `Asia/Shanghai`)

## 注意事项

- **异步任务**: ThreadLocal 无请求上下文，需手动设置/清理 TimeZoneContext
- **Date类型**: `java.util.Date` 内部是UTC时间戳，时区转换仅影响字符串表示
- **Converter**: QueryParam参数转换通过 `WebMvcConfig.addFormatters()` 注册
package com.campusoverflow.shared.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 数据库时间约定：所有 DATETIME(6) 列一律存 UTC 墙钟时间。
 * JPA 由 hibernate.jdbc.time_zone=UTC 保证；手写 JDBC 时统一用本工具转换，与 JVM 默认时区无关。
 */
public final class UtcTime {

    private UtcTime() {
    }

    public static LocalDateTime toDb(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static Instant fromDb(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

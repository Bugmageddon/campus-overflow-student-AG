package com.campusoverflow.qa.infrastructure.cache;

import com.campusoverflow.qa.application.port.ViewCounter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 浏览量：请求路径只做一次 Redis HINCRBY；定时任务把增量批量落库。
 * Redis 不可用时静默降级（丢失少量浏览数可以接受，但不能影响详情页可用性）。
 */
@Component
public class RedisViewCounter implements ViewCounter {

    private static final Logger log = LoggerFactory.getLogger(RedisViewCounter.class);
    static final String PENDING = "qa:views:pending";

    private final StringRedisTemplate redis;
    private final JdbcTemplate jdbc;

    public RedisViewCounter(StringRedisTemplate redis, JdbcTemplate jdbc) {
        this.redis = redis;
        this.jdbc = jdbc;
    }

    @Override
    public void increment(long questionId) {
        try {
            redis.opsForHash().increment(PENDING, String.valueOf(questionId), 1);
        } catch (RuntimeException e) {
            log.warn("浏览计数写入 Redis 失败，已降级忽略: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${co.qa.view-flush-interval-ms:60000}")
    public void flush() {
        String flushing = PENDING + ":" + UUID.randomUUID();
        try {
            if (!Boolean.TRUE.equals(redis.hasKey(PENDING))) {
                return;
            }
            redis.rename(PENDING, flushing); // 原子地“摘走”当前批次，新增量写入新的 PENDING
            Map<Object, Object> entries = redis.opsForHash().entries(flushing);
            List<Object[]> args = entries.entrySet().stream()
                    .map(e -> new Object[] {Long.parseLong(e.getValue().toString()), Long.parseLong(e.getKey().toString())})
                    .toList();
            jdbc.batchUpdate("UPDATE qa_question SET view_count = view_count + ? WHERE id = ?", args);
            redis.delete(flushing);
        } catch (RuntimeException e) {
            log.warn("浏览计数落库失败，将在下个周期重试: {}", e.getMessage());
        }
    }
}

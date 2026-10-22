package com.campusoverflow.bootstrap.outbox;

import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.util.UtcTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 发件箱中继：轮询未投递事件 → 反序列化 → 通过进程内事件总线（ApplicationEventPublisher）分发给订阅者。
 * <ul>
 *   <li>认领：SELECT ... FOR UPDATE SKIP LOCKED + 租约（locked_until），支持多实例并行而不重复认领；</li>
 *   <li>投递语义：至少一次。订阅者必须幂等（账本唯一键 / processed_event 表）；</li>
 *   <li>顺序：批内按 id 顺序投递，遇到失败即停止本批（避免后续事件越过失败事件），失败事件指数退避重试；</li>
 *   <li>超过最大重试次数即成为“死信”，由指标 co_outbox_dead_letters 暴露并告警，人工处理后可将 attempts 清零重放。</li>
 * </ul>
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final Duration LEASE = Duration.ofSeconds(30);

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    private final TransactionTemplate tx;
    private final ApplicationEventPublisher bus;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final int batchSize;
    private final int maxAttempts;
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
    private final Counter published;
    private final Counter failed;

    private record Row(long id, String eventId, String type, String payload) {
    }

    public OutboxRelay(JdbcTemplate jdbc, NamedParameterJdbcTemplate named, TransactionTemplate tx,
                       ApplicationEventPublisher bus, ObjectMapper mapper, Clock clock, MeterRegistry registry,
                       @Value("${co.outbox.batch-size:100}") int batchSize,
                       @Value("${co.outbox.max-attempts:10}") int maxAttempts) {
        this.jdbc = jdbc;
        this.named = named;
        this.tx = tx;
        this.bus = bus;
        this.mapper = mapper;
        this.clock = clock;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.published = registry.counter("co_outbox_published_total");
        this.failed = registry.counter("co_outbox_failed_total");
        Gauge.builder("co_outbox_backlog", this, r -> r.count("attempts < " + maxAttempts)).register(registry);
        Gauge.builder("co_outbox_dead_letters", this, r -> r.count("attempts >= " + maxAttempts)).register(registry);
    }

    @Scheduled(fixedDelayString = "${co.outbox.poll-interval-ms:500}")
    public void relay() {
        List<Row> batch = claim();
        for (Row row : batch) {
            if (!dispatch(row)) {
                release(batch, row);
                break;
            }
        }
    }

    private List<Row> claim() {
        List<Row> rows = tx.execute(status -> {
            LocalDateTime now = UtcTime.toDb(clock.instant());
            List<Long> ids = jdbc.queryForList("""
                    SELECT id FROM outbox_event
                    WHERE published_at IS NULL AND attempts < ? AND (locked_until IS NULL OR locked_until < ?)
                    ORDER BY id LIMIT ? FOR UPDATE SKIP LOCKED
                    """, Long.class, maxAttempts, now, batchSize);
            if (ids.isEmpty()) {
                return List.of();
            }
            MapSqlParameterSource p = new MapSqlParameterSource("ids", ids)
                    .addValue("until", UtcTime.toDb(clock.instant().plus(LEASE))).addValue("owner", instanceId);
            named.update("UPDATE outbox_event SET locked_until = :until, locked_by = :owner WHERE id IN (:ids)", p);
            return named.query("SELECT id, event_id, event_type, payload FROM outbox_event WHERE id IN (:ids) ORDER BY id",
                    p, (rs, i) -> new Row(rs.getLong("id"), rs.getString("event_id"), rs.getString("event_type"),
                            rs.getString("payload")));
        });
        return rows == null ? List.of() : rows;
    }

    private boolean dispatch(Row row) {
        try {
            Class<?> type = Class.forName(row.type());
            if (!IntegrationEvent.class.isAssignableFrom(type)) {
                throw new IllegalStateException("非法事件类型: " + row.type()); // 防御反序列化攻击
            }
            bus.publishEvent(mapper.readValue(row.payload(), type));
            jdbc.update("UPDATE outbox_event SET published_at = ?, locked_until = NULL, last_error = NULL WHERE id = ?",
                    UtcTime.toDb(clock.instant()), row.id());
            published.increment();
            return true;
        } catch (Exception e) {
            failed.increment();
            Integer attempts = jdbc.queryForObject("SELECT attempts FROM outbox_event WHERE id = ?", Integer.class, row.id());
            int next = (attempts == null ? 0 : attempts) + 1;
            Instant retryAt = clock.instant().plusSeconds(Math.min(1L << Math.min(next, 8), 300));
            String error = String.valueOf(e.getMessage());
            jdbc.update("UPDATE outbox_event SET attempts = ?, locked_until = ?, last_error = ? WHERE id = ?",
                    next, UtcTime.toDb(retryAt), error.length() > 500 ? error.substring(0, 500) : error, row.id());
            log.warn("事件投递失败 id={} type={} attempts={}: {}", row.id(), row.type(), next, error);
            return false;
        }
    }

    /** 本批中失败事件之后的行提前释放租约，下个周期重新认领（保证顺序）。 */
    private void release(List<Row> batch, Row failedRow) {
        List<Long> rest = batch.stream().map(Row::id).filter(id -> id > failedRow.id()).toList();
        if (!rest.isEmpty()) {
            named.update("UPDATE outbox_event SET locked_until = NULL WHERE id IN (:ids) AND published_at IS NULL",
                    new MapSqlParameterSource("ids", rest));
        }
    }

    /** 已投递事件保留 7 天，便于排查与重放。 */
    @Scheduled(cron = "${co.outbox.cleanup-cron:0 0 4 * * *}")
    public void cleanup() {
        int n = jdbc.update("DELETE FROM outbox_event WHERE published_at IS NOT NULL AND published_at < ?",
                UtcTime.toDb(clock.instant().minus(Duration.ofDays(7))));
        log.info("清理已投递的发件箱事件 {} 条", n);
    }

    private double count(String condition) {
        try {
            Long n = jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event WHERE published_at IS NULL AND "
                    + condition, Long.class);
            return n == null ? 0 : n;
        } catch (RuntimeException e) {
            return Double.NaN;
        }
    }
}

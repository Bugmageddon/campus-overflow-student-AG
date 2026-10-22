package com.campusoverflow.bootstrap.outbox;

import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.util.UtcTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事务性发件箱（ADR-002）的写端：事件与业务数据在同一个本地事务中落库。
 * Propagation.MANDATORY 把“必须在事务中发布”变成运行期强约束。
 */
@Component
public class OutboxIntegrationEventPublisher implements IntegrationEventPublisher {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final Clock clock;

    public OutboxIntegrationEventPublisher(JdbcTemplate jdbc, ObjectMapper mapper, Clock clock) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(IntegrationEvent event) {
        try {
            jdbc.update("""
                    INSERT INTO outbox_event (event_id, event_type, payload, occurred_at, created_at, attempts)
                    VALUES (?, ?, ?, ?, ?, 0)
                    """, event.eventId(), event.getClass().getName(), mapper.writeValueAsString(event),
                    UtcTime.toDb(event.occurredAt()), UtcTime.toDb(clock.instant()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("事件序列化失败: " + event.getClass().getSimpleName(), e);
        }
    }
}

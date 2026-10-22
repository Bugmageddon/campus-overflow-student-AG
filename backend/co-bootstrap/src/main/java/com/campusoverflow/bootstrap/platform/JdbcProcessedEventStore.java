package com.campusoverflow.bootstrap.platform;

import com.campusoverflow.shared.event.ProcessedEventStore;
import com.campusoverflow.shared.util.UtcTime;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 消费端幂等：INSERT IGNORE 返回 1 表示首次处理。必须与业务更新处于同一事务。 */
@Component
public class JdbcProcessedEventStore implements ProcessedEventStore {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public JdbcProcessedEventStore(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean markProcessed(String eventId, String consumer) {
        return jdbc.update("INSERT IGNORE INTO processed_event (event_id, consumer, processed_at) VALUES (?, ?, ?)",
                eventId, consumer, UtcTime.toDb(clock.instant())) == 1;
    }
}

package com.campusoverflow.bootstrap.platform;

import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.util.UtcTime;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 审计日志：只追加（应用账号在生产环境应仅授予 audit_log 的 INSERT/SELECT 权限）。 */
@Component
public class JdbcAuditTrail implements AuditTrail {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public JdbcAuditTrail(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void record(String action, Long actorId, String targetType, String targetId, String detail) {
        jdbc.update("""
                INSERT INTO audit_log (action, actor_id, target_type, target_id, detail, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, action, actorId, targetType, targetId,
                detail != null && detail.length() > 500 ? detail.substring(0, 500) : detail,
                UtcTime.toDb(clock.instant()));
    }
}

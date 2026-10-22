-- 平台级基础设施表：事务性发件箱、消费幂等、审计日志。不属于任何限界上下文。
CREATE TABLE outbox_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    event_id     VARCHAR(64)  NOT NULL,
    event_type   VARCHAR(200) NOT NULL COMMENT '事件类全限定名（用于反序列化）',
    payload      MEDIUMTEXT   NOT NULL COMMENT '事件 JSON',
    occurred_at  DATETIME(6)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    published_at DATETIME(6)  NULL COMMENT '非空表示已成功投递',
    attempts     INT          NOT NULL DEFAULT 0,
    locked_until DATETIME(6)  NULL COMMENT '租约到期时间，防止多实例重复投递',
    locked_by    VARCHAR(40)  NULL,
    last_error   VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_event_id (event_id),
    KEY idx_outbox_pending (published_at, attempts, locked_until, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE processed_event (
    event_id     VARCHAR(64) NOT NULL,
    consumer     VARCHAR(64) NOT NULL COMMENT '消费者标识，同一事件可被多个消费者各处理一次',
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id, consumer)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    action      VARCHAR(64)  NOT NULL,
    actor_id    BIGINT       NULL,
    target_type VARCHAR(32)  NULL,
    target_id   VARCHAR(64)  NULL,
    detail      VARCHAR(500) NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_actor (actor_id, created_at),
    KEY idx_audit_action (action, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

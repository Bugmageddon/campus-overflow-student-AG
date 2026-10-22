-- Reputation 上下文（rep_* 表）
CREATE TABLE rep_account (
    user_id          BIGINT NOT NULL,
    reputation       INT    NOT NULL DEFAULT 0 COMMENT '恒等于 rep_ledger 中该用户 delta 之和',
    available_points INT    NOT NULL DEFAULT 0 COMMENT '可用悬赏积分',
    frozen_points    INT    NOT NULL DEFAULT 0 COMMENT '冻结中的悬赏积分',
    version          BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    KEY idx_account_reputation (reputation)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE rep_ledger (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    event_id   VARCHAR(64) NOT NULL COMMENT '来源事件 ID（幂等键）',
    reason     VARCHAR(32) NOT NULL,
    delta      INT         NOT NULL,
    ref_type   VARCHAR(16) NULL,
    ref_id     BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ledger_event (event_id, user_id, reason),
    KEY idx_ledger_user (user_id, id),
    KEY idx_ledger_user_reason (user_id, reason, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE rep_bounty (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    question_id BIGINT      NOT NULL,
    sponsor_id  BIGINT      NOT NULL,
    points      INT         NOT NULL,
    status      VARCHAR(16) NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    winner_id   BIGINT      NULL,
    answer_id   BIGINT      NULL,
    closed_at   DATETIME(6) NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_bounty_question (question_id, status),
    KEY idx_bounty_due (status, expires_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE rep_badge (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    badge_code VARCHAR(32) NOT NULL,
    awarded_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_badge (user_id, badge_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

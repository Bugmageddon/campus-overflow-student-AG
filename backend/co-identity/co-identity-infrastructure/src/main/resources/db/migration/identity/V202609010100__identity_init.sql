-- Identity 上下文：只允许本上下文读写 id_* 表（禁止其他上下文 JOIN / 外键引用）
CREATE TABLE id_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(20)  NOT NULL COMMENT '学号/工号（个人信息，不对外展示）',
    display_name  VARCHAR(20)  NOT NULL COMMENT '公开昵称，用于 @提及',
    email_cipher  VARCHAR(255) NOT NULL COMMENT 'AES-GCM 密文',
    email_hash    VARCHAR(64)  NOT NULL COMMENT 'HMAC-SHA256 摘要，用于唯一性',
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(16)  NOT NULL,
    status        VARCHAR(16)  NOT NULL,
    verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    college       VARCHAR(50)  NULL,
    class_name    VARCHAR(50)  NULL,
    created_at    DATETIME(6) NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_display_name (display_name),
    UNIQUE KEY uk_user_email_hash (email_hash),
    KEY idx_user_verified (verified, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE id_course (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(20)  NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    term       VARCHAR(20)  NOT NULL,
    teacher_id BIGINT       NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code_term (code, term)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE id_course_member (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    course_id BIGINT       NOT NULL,
    user_id   BIGINT       NOT NULL,
    role      VARCHAR(16)  NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member (course_id, user_id),
    KEY idx_member_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

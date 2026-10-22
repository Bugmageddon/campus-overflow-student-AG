-- Q&A 上下文（qa_* 表）。只存 Markdown 原文；计数器为反规范化字段，由原子 UPDATE 维护。
CREATE TABLE qa_question (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    author_id          BIGINT       NOT NULL COMMENT '逻辑引用 id_user.id（跨上下文，不建外键）',
    course_id          BIGINT       NULL COMMENT '逻辑引用 id_course.id',
    title              VARCHAR(150) NOT NULL,
    body               MEDIUMTEXT   NOT NULL,
    accepted_answer_id BIGINT       NULL,
    score              INT          NOT NULL DEFAULT 0,
    answer_count       INT          NOT NULL DEFAULT 0,
    view_count         INT          NOT NULL DEFAULT 0,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    deleted_at         DATETIME(6)  NULL COMMENT '软删除（ADR-006）',
    version            BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_question_created (deleted_at, created_at),
    KEY idx_question_course (course_id, deleted_at, created_at),
    KEY idx_question_author (author_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE qa_question_tag (
    question_id BIGINT      NOT NULL,
    tag         VARCHAR(30) NOT NULL,
    PRIMARY KEY (question_id, tag),
    KEY idx_question_tag_tag (tag)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE qa_tag (
    name           VARCHAR(30) NOT NULL,
    question_count INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (name),
    KEY idx_tag_count (question_count)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE qa_answer (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    question_id BIGINT      NOT NULL,
    author_id   BIGINT      NOT NULL,
    body        MEDIUMTEXT  NOT NULL,
    score       INT         NOT NULL DEFAULT 0,
    endorsed_by BIGINT      NULL COMMENT '教师认证人',
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    deleted_at  DATETIME(6) NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_answer_question (question_id, deleted_at),
    KEY idx_answer_author (author_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE qa_comment (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    target_type VARCHAR(16)  NOT NULL,
    target_id   BIGINT       NOT NULL,
    question_id BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL,
    parent_id   BIGINT       NULL,
    body        VARCHAR(600) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_comment_question (question_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE qa_vote (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    voter_id    BIGINT      NOT NULL,
    target_type VARCHAR(16) NOT NULL,
    target_id   BIGINT      NOT NULL,
    value       INT         NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vote (voter_id, target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

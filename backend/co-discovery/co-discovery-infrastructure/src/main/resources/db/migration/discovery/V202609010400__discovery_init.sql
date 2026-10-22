-- Discovery 上下文（dis_* 表）：搜索投影 + 通知。
-- ngram 分词会丢弃“包含停用词”的词元（默认停用词含 a、i 等），会导致英文检索失效，
-- 因此在创建全文索引前关闭停用词（该设置在建索引时固化到索引上）。
SET SESSION innodb_ft_enable_stopword = 0;

CREATE TABLE dis_search_doc (
    question_id      BIGINT       NOT NULL,
    course_id        BIGINT       NULL,
    author_id        BIGINT       NOT NULL,
    title            VARCHAR(150) NOT NULL,
    body_text        TEXT         NOT NULL COMMENT 'Markdown 转纯文本后的正文（截断至 2000 字）',
    tag_count        INT          NOT NULL DEFAULT 0,
    score            INT          NOT NULL DEFAULT 0,
    answer_count     INT          NOT NULL DEFAULT 0,
    accepted         BOOLEAN      NOT NULL DEFAULT FALSE,
    bounty_open      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME(6)  NOT NULL,
    last_activity_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (question_id),
    KEY idx_doc_created (deleted, created_at),
    KEY idx_doc_course (course_id, deleted, created_at),
    KEY idx_doc_activity (deleted, last_activity_at),
    KEY idx_doc_score (deleted, score),
    FULLTEXT KEY ft_doc (title, body_text) WITH PARSER ngram
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE dis_question_tag (
    question_id BIGINT      NOT NULL,
    tag         VARCHAR(30) NOT NULL,
    PRIMARY KEY (question_id, tag),
    KEY idx_dis_tag (tag)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE dis_notification (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_id    BIGINT       NOT NULL,
    type            VARCHAR(32)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    link            VARCHAR(200) NULL,
    source_event_id VARCHAR(64)  NOT NULL,
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_event (source_event_id, recipient_id),
    KEY idx_notification_recipient (recipient_id, is_read, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

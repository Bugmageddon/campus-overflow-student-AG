package com.campusoverflow.qa.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "qa_comment")
public class CommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private long targetId;

    @Column(name = "question_id", nullable = false)
    private long questionId;

    @Column(name = "author_id", nullable = false)
    private long authorId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 600)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {
    }

    public CommentJpaEntity(String targetType, long targetId, long questionId, long authorId, Long parentId,
                            String body, Instant createdAt) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.questionId = questionId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTargetType() { return targetType; }
    public long getTargetId() { return targetId; }
    public long getQuestionId() { return questionId; }
    public long getAuthorId() { return authorId; }
    public Long getParentId() { return parentId; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}

package com.campusoverflow.qa.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "qa_answer")
public class AnswerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private long questionId;

    @Column(name = "author_id", nullable = false)
    private long authorId;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @Column(nullable = false)
    private int score;

    @Column(name = "endorsed_by")
    private Long endorsedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    protected AnswerJpaEntity() {
    }

    public AnswerJpaEntity(long questionId, long authorId, Instant createdAt) {
        this.questionId = questionId;
        this.authorId = authorId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public long getQuestionId() { return questionId; }
    public long getAuthorId() { return authorId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public int getScore() { return score; }
    public Long getEndorsedBy() { return endorsedBy; }
    public void setEndorsedBy(Long endorsedBy) { this.endorsedBy = endorsedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public long getVersion() { return version; }
}

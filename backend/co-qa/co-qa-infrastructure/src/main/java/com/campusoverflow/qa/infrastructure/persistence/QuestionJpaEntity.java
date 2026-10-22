package com.campusoverflow.qa.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "qa_question")
public class QuestionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private long authorId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @ElementCollection
    @CollectionTable(name = "qa_question_tag", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "tag", length = 30, nullable = false)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(nullable = false)
    private int score;

    @Column(name = "answer_count", nullable = false)
    private int answerCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    protected QuestionJpaEntity() {
    }

    public QuestionJpaEntity(long authorId, Long courseId, Instant createdAt) {
        this.authorId = authorId;
        this.courseId = courseId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public long getAuthorId() { return authorId; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Set<String> getTags() { return tags; }
    public Long getAcceptedAnswerId() { return acceptedAnswerId; }
    public void setAcceptedAnswerId(Long acceptedAnswerId) { this.acceptedAnswerId = acceptedAnswerId; }
    public int getScore() { return score; }
    public int getAnswerCount() { return answerCount; }
    public int getViewCount() { return viewCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public long getVersion() { return version; }
}

package com.campusoverflow.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "id_course")
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String term;

    @Column(name = "teacher_id", nullable = false)
    private long teacherId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CourseJpaEntity() {
    }

    public CourseJpaEntity(String code, String name, String term, long teacherId, Instant createdAt) {
        this.code = code;
        this.name = name;
        this.term = term;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getTerm() { return term; }
    public long getTeacherId() { return teacherId; }
    public Instant getCreatedAt() { return createdAt; }
}

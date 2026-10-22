package com.campusoverflow.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "id_course_member")
public class CourseMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private long courseId;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(nullable = false, length = 16)
    private String role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected CourseMemberJpaEntity() {
    }

    public CourseMemberJpaEntity(long courseId, long userId, String role, Instant joinedAt) {
        this.courseId = courseId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public long getCourseId() { return courseId; }
    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getJoinedAt() { return joinedAt; }
}

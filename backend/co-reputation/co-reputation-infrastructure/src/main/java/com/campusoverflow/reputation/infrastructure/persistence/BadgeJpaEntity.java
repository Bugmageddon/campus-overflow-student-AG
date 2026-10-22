package com.campusoverflow.reputation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rep_badge")
public class BadgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "badge_code", nullable = false, length = 32)
    private String badgeCode;

    @Column(name = "awarded_at", nullable = false)
    private Instant awardedAt;

    protected BadgeJpaEntity() {
    }

    public BadgeJpaEntity(long userId, String badgeCode, Instant awardedAt) {
        this.userId = userId;
        this.badgeCode = badgeCode;
        this.awardedAt = awardedAt;
    }

    public long getUserId() { return userId; }
    public String getBadgeCode() { return badgeCode; }
    public Instant getAwardedAt() { return awardedAt; }
}

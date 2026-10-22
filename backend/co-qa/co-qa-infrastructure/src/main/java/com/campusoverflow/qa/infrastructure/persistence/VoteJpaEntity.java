package com.campusoverflow.qa.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 唯一约束 uk_vote(voter_id, target_type, target_id) 兜底并发重复投票。 */
@Entity
@Table(name = "qa_vote")
public class VoteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voter_id", nullable = false)
    private long voterId;

    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private long targetId;

    @Column(nullable = false)
    private int value;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VoteJpaEntity() {
    }

    public VoteJpaEntity(long voterId, String targetType, long targetId) {
        this.voterId = voterId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public Long getId() { return id; }
    public long getVoterId() { return voterId; }
    public String getTargetType() { return targetType; }
    public long getTargetId() { return targetId; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

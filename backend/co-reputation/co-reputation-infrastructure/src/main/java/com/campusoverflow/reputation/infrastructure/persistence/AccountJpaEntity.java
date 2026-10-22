package com.campusoverflow.reputation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "rep_account")
public class AccountJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private int reputation;

    @Column(name = "available_points", nullable = false)
    private int availablePoints;

    @Column(name = "frozen_points", nullable = false)
    private int frozenPoints;

    @Version
    private Long version;

    protected AccountJpaEntity() {
    }

    public AccountJpaEntity(long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public int getReputation() { return reputation; }
    public void setReputation(int reputation) { this.reputation = reputation; }
    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = availablePoints; }
    public int getFrozenPoints() { return frozenPoints; }
    public void setFrozenPoints(int frozenPoints) { this.frozenPoints = frozenPoints; }
    public long getVersion() { return version == null ? 0 : version; }
}

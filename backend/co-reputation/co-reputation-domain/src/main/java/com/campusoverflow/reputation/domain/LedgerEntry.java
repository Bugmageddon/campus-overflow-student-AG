package com.campusoverflow.reputation.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 声誉账本流水（只追加，ADR-004）。(eventId, userId, reason) 唯一，使事件重复投递时天然幂等。
 */
public record LedgerEntry(Long id, long userId, String eventId, ReputationReason reason, int delta, String refType,
                          long refId, Instant createdAt) {

    public LedgerEntry {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(reason);
    }

    public static LedgerEntry of(long userId, String eventId, ReputationReason reason, int delta, String refType,
                                 long refId, Instant now) {
        return new LedgerEntry(null, userId, eventId, reason, delta, refType, refId, now);
    }
}

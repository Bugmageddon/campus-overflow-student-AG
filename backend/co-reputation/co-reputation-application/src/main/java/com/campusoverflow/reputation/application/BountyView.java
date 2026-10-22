package com.campusoverflow.reputation.application;

import java.time.Instant;

public record BountyView(long id, long questionId, long sponsorId, String sponsorName, int points, String status,
                         Instant createdAt, Instant expiresAt, Long winnerId, Long answerId) {
}

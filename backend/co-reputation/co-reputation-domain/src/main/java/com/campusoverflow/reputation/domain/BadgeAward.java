package com.campusoverflow.reputation.domain;

import java.time.Instant;

public record BadgeAward(long userId, BadgeType type, Instant awardedAt) {
}

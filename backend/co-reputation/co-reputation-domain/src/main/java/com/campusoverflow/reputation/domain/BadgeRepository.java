package com.campusoverflow.reputation.domain;

import java.util.List;

public interface BadgeRepository {
    boolean has(long userId, BadgeType type);

    void award(BadgeAward award);

    List<BadgeAward> findByUser(long userId);
}

package com.campusoverflow.reputation.application;

import java.time.Instant;
import java.util.List;

/** 声誉档案。积分字段仅本人可见，他人查看时为 null。 */
public record ReputationProfileView(long userId, String displayName, int reputation, Integer availablePoints,
                                    Integer frozenPoints, List<BadgeView> badges) {

    public record BadgeView(String code, String name, String description, Instant awardedAt) {
    }
}

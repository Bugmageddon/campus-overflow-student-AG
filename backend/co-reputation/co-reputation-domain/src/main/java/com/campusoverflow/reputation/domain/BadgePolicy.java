package com.campusoverflow.reputation.domain;

import java.util.EnumSet;
import java.util.Set;

/** 徽章授予规则（纯函数，易于测试与扩展）。 */
public final class BadgePolicy {

    private BadgePolicy() {
    }

    public static Set<BadgeType> forAcceptedCount(long acceptedCount) {
        Set<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (acceptedCount >= 1) {
            badges.add(BadgeType.FIRST_ACCEPTED);
        }
        if (acceptedCount >= 10) {
            badges.add(BadgeType.HELPER_10);
        }
        return badges;
    }

    public static Set<BadgeType> forReputation(int reputation) {
        Set<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (reputation >= 1000) {
            badges.add(BadgeType.REPUTATION_1000);
        }
        if (reputation >= 100) {
            badges.add(BadgeType.REPUTATION_100);
        }
        return badges;
    }
}

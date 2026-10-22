package com.campusoverflow.reputation.domain;

import java.util.EnumSet;
import java.util.Set;

/** 徽章授予规则（纯函数，易于测试与扩展）。 */
public final class BadgePolicy {

    private BadgePolicy() {
    }

    public static Set<BadgeType> forAcceptedCount(long acceptedCount) {
        // TODO(S3)：实现 BadgePolicy.forAcceptedCount——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：BadgePolicy.forAcceptedCount 尚未实现");
    }

    public static Set<BadgeType> forReputation(int reputation) {
        // TODO(S3)：实现 BadgePolicy.forReputation——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：BadgePolicy.forReputation 尚未实现");
    }
}

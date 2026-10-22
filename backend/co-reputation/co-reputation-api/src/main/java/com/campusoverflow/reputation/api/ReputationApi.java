package com.campusoverflow.reputation.api;

import java.util.Collection;
import java.util.Map;

/** Reputation 上下文对外查询接口。 */
public interface ReputationApi {
    ReputationSummary summary(long userId);

    Map<Long, Integer> reputations(Collection<Long> userIds);
}

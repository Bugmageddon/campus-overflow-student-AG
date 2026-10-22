package com.campusoverflow.reputation.domain;

import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.time.Instant;
import java.util.Map;

public interface LedgerRepository {
    boolean exists(String eventId, long userId, ReputationReason reason);

    void append(LedgerEntry entry);

    PageResult<LedgerEntry> findByUser(long userId, PageRequest page);

    long countByUserAndReason(long userId, ReputationReason reason);

    /** 某用户自 since 起因被投票获得的正向声誉总和（用于每日上限）。 */
    int sumPositiveVoteDeltaSince(long userId, Instant since);

    /** 对账：每个用户的流水总和。 */
    Map<Long, Long> sumByUser();
}

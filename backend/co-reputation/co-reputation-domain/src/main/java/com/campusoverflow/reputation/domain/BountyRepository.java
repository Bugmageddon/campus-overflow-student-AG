package com.campusoverflow.reputation.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BountyRepository {
    Optional<Bounty> findOpenByQuestion(long questionId);

    Optional<Bounty> findLatestByQuestion(long questionId);

    List<Bounty> findDue(Instant now, int limit);

    Bounty save(Bounty bounty);
}

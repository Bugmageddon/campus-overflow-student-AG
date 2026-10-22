package com.campusoverflow.reputation.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReputationAccountRepository {
    Optional<ReputationAccount> findByUserId(long userId);

    List<ReputationAccount> findAllByUserId(Collection<Long> userIds);

    List<ReputationAccount> topByReputation(int limit);

    List<ReputationAccount> findAll();

    ReputationAccount save(ReputationAccount account);
}

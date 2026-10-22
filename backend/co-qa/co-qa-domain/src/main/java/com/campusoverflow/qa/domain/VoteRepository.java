package com.campusoverflow.qa.domain;

import java.util.Optional;

public interface VoteRepository {
    Optional<Vote> find(long voterId, TargetType targetType, long targetId);

    Vote save(Vote vote);

    void delete(Vote vote);
}

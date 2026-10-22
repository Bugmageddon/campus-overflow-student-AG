package com.campusoverflow.qa.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long> {
    Optional<VoteJpaEntity> findByVoterIdAndTargetTypeAndTargetId(long voterId, String targetType, long targetId);
}

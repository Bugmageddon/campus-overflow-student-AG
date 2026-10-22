package com.campusoverflow.reputation.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BountyJpaRepository extends JpaRepository<BountyJpaEntity, Long> {

    Optional<BountyJpaEntity> findFirstByQuestionIdAndStatus(long questionId, String status);

    Optional<BountyJpaEntity> findFirstByQuestionIdOrderByIdDesc(long questionId);

    List<BountyJpaEntity> findByStatusAndExpiresAtLessThanEqualOrderByExpiresAt(String status, Instant now,
                                                                                 Pageable pageable);
}

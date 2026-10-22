package com.campusoverflow.reputation.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeJpaRepository extends JpaRepository<BadgeJpaEntity, Long> {

    boolean existsByUserIdAndBadgeCode(long userId, String badgeCode);

    List<BadgeJpaEntity> findByUserIdOrderByAwardedAt(long userId);
}

package com.campusoverflow.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseMemberJpaRepository extends JpaRepository<CourseMemberJpaEntity, Long> {
    Optional<CourseMemberJpaEntity> findByCourseIdAndUserId(long courseId, long userId);

    List<CourseMemberJpaEntity> findByCourseId(long courseId);

    List<CourseMemberJpaEntity> findByUserId(long userId);
}

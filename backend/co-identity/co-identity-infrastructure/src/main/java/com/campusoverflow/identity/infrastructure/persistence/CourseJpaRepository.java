package com.campusoverflow.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository extends JpaRepository<CourseJpaEntity, Long> {
    boolean existsByCodeAndTerm(String code, String term);
}

package com.campusoverflow.qa.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {
}

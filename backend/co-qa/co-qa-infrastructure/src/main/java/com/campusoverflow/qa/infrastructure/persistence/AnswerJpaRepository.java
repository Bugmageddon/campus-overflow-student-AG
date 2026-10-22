package com.campusoverflow.qa.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerJpaRepository extends JpaRepository<AnswerJpaEntity, Long> {

    @Modifying
    @Query("update AnswerJpaEntity a set a.score = a.score + :delta where a.id = :id")
    int adjustScore(@Param("id") long id, @Param("delta") int delta);
}

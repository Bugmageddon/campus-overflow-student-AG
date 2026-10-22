package com.campusoverflow.reputation.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerJpaRepository extends JpaRepository<LedgerJpaEntity, Long> {

    boolean existsByEventIdAndUserIdAndReason(String eventId, long userId, String reason);

    Page<LedgerJpaEntity> findByUserIdOrderByIdDesc(long userId, Pageable pageable);

    long countByUserIdAndReason(long userId, String reason);

    @Query("""
            select coalesce(sum(l.delta), 0) from LedgerJpaEntity l
            where l.userId = :userId and l.delta > 0 and l.reason in :reasons and l.createdAt >= :since
            """)
    Long sumPositiveSince(@Param("userId") long userId, @Param("reasons") Collection<String> reasons,
                          @Param("since") Instant since);

    @Query("select l.userId, sum(l.delta) from LedgerJpaEntity l group by l.userId")
    List<Object[]> sumGroupByUser();
}

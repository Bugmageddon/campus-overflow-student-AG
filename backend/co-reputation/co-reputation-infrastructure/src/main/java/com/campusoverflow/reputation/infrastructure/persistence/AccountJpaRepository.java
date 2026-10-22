package com.campusoverflow.reputation.infrastructure.persistence;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

    @Query("select a from AccountJpaEntity a order by a.reputation desc, a.userId asc")
    List<AccountJpaEntity> findTop(Pageable pageable);
}

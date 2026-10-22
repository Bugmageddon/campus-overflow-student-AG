package com.campusoverflow.identity.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByDisplayName(String displayName);

    boolean existsByUsername(String username);

    boolean existsByDisplayName(String displayName);

    boolean existsByEmailHash(String emailHash);

    Page<UserJpaEntity> findByVerified(boolean verified, Pageable pageable);
}

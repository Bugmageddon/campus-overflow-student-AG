package com.campusoverflow.discovery.infrastructure.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    boolean existsBySourceEventIdAndRecipientId(String sourceEventId, long recipientId);

    Page<NotificationJpaEntity> findByRecipientIdOrderByIdDesc(long recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(long recipientId);

    @Modifying
    @Query("update NotificationJpaEntity n set n.read = true where n.recipientId = :recipientId and n.read = false")
    int markAllRead(@Param("recipientId") long recipientId);
}

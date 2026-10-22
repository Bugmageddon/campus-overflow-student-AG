package com.campusoverflow.discovery.infrastructure.notification;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.discovery.domain.notification.NotificationType;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpa;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean exists(String sourceEventId, long recipientId) {
        return jpa.existsBySourceEventIdAndRecipientId(sourceEventId, recipientId);
    }

    @Override
    public Notification save(Notification n) {
        NotificationJpaEntity e = n.id() == null
                ? new NotificationJpaEntity(n.recipientId(), n.type().name(), n.title(), n.link(), n.sourceEventId(),
                n.createdAt())
                : jpa.findById(n.id()).orElseThrow();
        e.setRead(n.read());
        NotificationJpaEntity saved = jpa.save(e);
        if (n.id() == null) {
            n.assignId(saved.getId());
        }
        return n;
    }

    @Override
    public Optional<Notification> findById(long id) {
        return jpa.findById(id).map(NotificationRepositoryAdapter::toDomain);
    }

    @Override
    public PageResult<Notification> findByRecipient(long recipientId, PageRequest page) {
        Page<NotificationJpaEntity> p = jpa.findByRecipientIdOrderByIdDesc(recipientId,
                org.springframework.data.domain.PageRequest.of(page.page() - 1, page.size()));
        return new PageResult<>(p.getContent().stream().map(NotificationRepositoryAdapter::toDomain).toList(),
                p.getTotalElements(), page.page(), page.size());
    }

    @Override
    public long countUnread(long recipientId) {
        return jpa.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    public int markAllRead(long recipientId) {
        return jpa.markAllRead(recipientId);
    }

    private static Notification toDomain(NotificationJpaEntity e) {
        return Notification.reconstitute(e.getId(), e.getRecipientId(), NotificationType.valueOf(e.getType()),
                e.getTitle(), e.getLink(), e.getSourceEventId(), e.isRead(), e.getCreatedAt());
    }
}

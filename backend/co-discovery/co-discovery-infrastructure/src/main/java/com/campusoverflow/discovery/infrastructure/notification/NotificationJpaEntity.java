package com.campusoverflow.discovery.infrastructure.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "dis_notification")
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private long recipientId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String link;

    @Column(name = "source_event_id", nullable = false, length = 64)
    private String sourceEventId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationJpaEntity() {
    }

    public NotificationJpaEntity(long recipientId, String type, String title, String link, String sourceEventId,
                                 Instant createdAt) {
        this.recipientId = recipientId;
        this.type = type;
        this.title = title;
        this.link = link;
        this.sourceEventId = sourceEventId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public long getRecipientId() { return recipientId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getSourceEventId() { return sourceEventId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getCreatedAt() { return createdAt; }
}

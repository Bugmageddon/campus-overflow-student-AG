package com.campusoverflow.discovery.domain.notification;

import com.campusoverflow.shared.domain.ForbiddenException;
import java.time.Instant;
import java.util.Objects;

/** 站内通知。(sourceEventId, recipientId) 唯一，事件重复投递不会产生重复通知。 */
public class Notification {

    private Long id;
    private final long recipientId;
    private final NotificationType type;
    private final String title;
    private final String link;
    private final String sourceEventId;
    private boolean read;
    private final Instant createdAt;

    private Notification(Long id, long recipientId, NotificationType type, String title, String link,
                         String sourceEventId, boolean read, Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = Objects.requireNonNull(type);
        this.title = title;
        this.link = link;
        this.sourceEventId = Objects.requireNonNull(sourceEventId);
        this.read = read;
        this.createdAt = createdAt;
    }

    public static Notification create(long recipientId, NotificationType type, String title, String link,
                                      String sourceEventId, Instant now) {
        // TODO(S3)：实现 Notification.create——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Notification.create 尚未实现");
    }

    public static Notification reconstitute(Long id, long recipientId, NotificationType type, String title,
                                            String link, String sourceEventId, boolean read, Instant createdAt) {
        return new Notification(id, recipientId, type, title, link, sourceEventId, read, createdAt);
    }

    public void markRead(long actorId) {
        // TODO(S3)：实现 Notification.markRead——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：Notification.markRead 尚未实现");
    }

    public void assignId(long id) {
        this.id = id;
    }

    public Long id() { return id; }
    public long recipientId() { return recipientId; }
    public NotificationType type() { return type; }
    public String title() { return title; }
    public String link() { return link; }
    public String sourceEventId() { return sourceEventId; }
    public boolean read() { return read; }
    public Instant createdAt() { return createdAt; }
}

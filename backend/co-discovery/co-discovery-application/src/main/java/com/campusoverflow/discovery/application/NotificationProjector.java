package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.notification.MentionParser;
import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.discovery.domain.notification.NotificationType;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.reputation.api.event.BadgeAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 通知中心：把上游事件转换为站内通知。幂等键 (sourceEventId, recipientId)。 */
@Component
public class NotificationProjector {

    private final NotificationRepository notifications;
    private final IdentityApi identity;
    private final QaQueryApi qa;

    public NotificationProjector(NotificationRepository notifications, IdentityApi identity, QaQueryApi qa) {
        this.notifications = notifications;
        this.identity = identity;
        this.qa = qa;
    }

    @EventListener
    @Transactional
    public void on(AnswerSubmittedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    @EventListener
    @Transactional
    public void on(AnswerEndorsedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    @EventListener
    @Transactional
    public void on(CommentPostedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    @EventListener
    @Transactional
    public void on(BountyAwardedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    @EventListener
    @Transactional
    public void on(BadgeAwardedEvent e) {
        // TODO(S3)：实现 NotificationProjector.on——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.on 尚未实现");
    }

    private void notify(long recipientId, NotificationType type, String title, String link, String eventId,
                        java.time.Instant at) {
        // TODO(S3)：实现 NotificationProjector.notify——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.notify 尚未实现");
    }

    private String name(long userId) {
        // TODO(S3)：实现 NotificationProjector.name——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationProjector.name 尚未实现");
    }
}

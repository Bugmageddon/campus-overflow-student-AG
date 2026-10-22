package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：通知中心（列表、未读数、已读）。 */
@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public PageResult<NotificationView> list(Actor actor, PageRequest page) {
        // TODO(S3)：实现 NotificationService.list——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationService.list 尚未实现");
    }

    @Transactional(readOnly = true)
    public long unreadCount(Actor actor) {
        // TODO(S3)：实现 NotificationService.unreadCount——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationService.unreadCount 尚未实现");
    }

    @Transactional
    public void markRead(Actor actor, long notificationId) {
        // TODO(S3)：实现 NotificationService.markRead——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationService.markRead 尚未实现");
    }

    @Transactional
    public int markAllRead(Actor actor) {
        // TODO(S3)：实现 NotificationService.markAllRead——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationService.markAllRead 尚未实现");
    }

    private static NotificationView toView(Notification n) {
        // TODO(S3)：实现 NotificationService.toView——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：NotificationService.toView 尚未实现");
    }
}

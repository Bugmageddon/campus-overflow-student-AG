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
        // 自问自答不通知
        if (e.answerAuthorId() == e.questionAuthorId()) {
            return;
        }
        notify(e.questionAuthorId(), NotificationType.ANSWER_RECEIVED,
                name(e.answerAuthorId()) + " 回答了你的问题「" + e.questionTitle() + "」",
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        // 自己采纳自己的回答不通知
        if (e.acceptedById() == e.answerAuthorId()) {
            return;
        }
        notify(e.answerAuthorId(), NotificationType.ANSWER_ACCEPTED,
                "你的回答被采纳「" + e.questionTitle() + "」", "/questions/" + e.questionId(), e.eventId(),
                e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(AnswerEndorsedEvent e) {
        // 自己认证自己的回答不通知
        if (e.endorserId() == e.answerAuthorId()) {
            return;
        }
        notify(e.answerAuthorId(), NotificationType.ANSWER_ENDORSED,
                name(e.endorserId()) + " 认证了你的回答「" + e.questionTitle() + "」",
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(CommentPostedEvent e) {
        for (String mention : MentionParser.extract(e.body())) {
            Optional<UserSummary> user = identity.findByDisplayName(mention);
            // 查无此人或与评论作者相同，跳过
            if (user.isEmpty() || user.get().userId() == e.authorId()) {
                continue;
            }
            notify(user.get().userId(), NotificationType.MENTIONED,
                    name(e.authorId()) + " 在评论中提到了你", "/questions/" + e.questionId(), e.eventId(),
                    e.occurredAt());
        }
    }

    @EventListener
    @Transactional
    public void on(BountyAwardedEvent e) {
        String title = qa.findQuestion(e.questionId()).map(QuestionRef::title).orElse("问题");
        notify(e.winnerId(), NotificationType.BOUNTY_AWARDED,
                "你赢得了问题「" + title + "」的悬赏 " + e.points() + " 分",
                "/questions/" + e.questionId(), e.eventId(), e.occurredAt());
    }

    @EventListener
    @Transactional
    public void on(BadgeAwardedEvent e) {
        notify(e.userId(), NotificationType.BADGE_AWARDED, "你获得了徽章「" + e.badgeName() + "」",
                "/reputation", e.eventId(), e.occurredAt());
    }

    private void notify(long recipientId, NotificationType type, String title, String link, String eventId,
                        java.time.Instant at) {
        if (notifications.exists(eventId, recipientId)) {
            return;
        }
        notifications.save(Notification.create(recipientId, type, title, link, eventId, at));
    }

    private String name(long userId) {
        return identity.findUser(userId).map(UserSummary::displayName).orElse("某同学");
    }
}

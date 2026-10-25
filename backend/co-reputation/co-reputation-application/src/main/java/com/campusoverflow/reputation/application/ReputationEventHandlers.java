package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.domain.BadgePolicy;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.reputation.domain.ReputationRulebook;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订阅上游上下文的集成事件（由 OutboxRelay 在事务提交后投递，至少一次语义）。
 * 每个处理器运行在自己的事务中，并通过账本唯一键 / 徽章存在性检查实现幂等。
 */
@Component
public class ReputationEventHandlers {

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;
    private final ReputationLedgerService ledgerService;
    private final BadgeService badges;
    private final BountyService bounties;
    private final Clock clock;

    public ReputationEventHandlers(ReputationAccountRepository accounts, LedgerRepository ledger,
                                   ReputationLedgerService ledgerService, BadgeService badges,
                                   BountyService bounties, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.ledgerService = ledgerService;
        this.badges = badges;
        this.bounties = bounties;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(UserRegisteredEvent e) {
        if (accounts.findByUserId(e.userId()).isPresent()) {
            return;
        }
        accounts.save(ReputationAccount.open(e.userId(), ReputationRulebook.initialPoints(e.role())));
    }

    /** 场景 R-2 的异步后半段：采纳 → 回答者 +15 → 徽章 → 悬赏结算。 */
    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        int delta = ReputationRulebook.acceptedDelta(e.answerAuthorId(), e.questionAuthorId());
        ledgerService.record(e.answerAuthorId(), e.eventId(), ReputationReason.ANSWER_ACCEPTED, delta, "ANSWER",
                e.answerId());
        long acceptedCount = ledger.countByUserAndReason(e.answerAuthorId(), ReputationReason.ANSWER_ACCEPTED);
        BadgePolicy.forAcceptedCount(acceptedCount)
                .forEach(b -> badges.grant(e.answerAuthorId(), b));
        bounties.settleOnAccept(e.questionId(), e.answerId(), e.answerAuthorId(), e.eventId());
    }

    @EventListener
    @Transactional
    public void on(VoteCastEvent e) {
        int delta = ReputationRulebook.voteDelta(e.oldValue(), e.newValue());
        ReputationReason reason = "QUESTION".equals(e.targetType())
                ? ReputationReason.QUESTION_VOTE : ReputationReason.ANSWER_VOTE;
        if (delta > 0) {
            Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
            int gainedToday = ledger.sumPositiveVoteDeltaSince(e.targetAuthorId(), dayStart);
            delta = ReputationRulebook.applyDailyCap(delta, gainedToday);
        }
        ledgerService.record(e.targetAuthorId(), e.eventId(), reason, delta, e.targetType(), e.targetId());
    }

    @EventListener
    @Transactional
    public void on(AnswerSubmittedEvent e) {
        badges.grant(e.answerAuthorId(), BadgeType.FIRST_ANSWER);
    }

    @EventListener
    @Transactional
    public void on(AnswerEndorsedEvent e) {
        badges.grant(e.answerAuthorId(), BadgeType.TEACHER_ENDORSED);
    }
}

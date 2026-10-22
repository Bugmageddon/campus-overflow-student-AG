package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.BadgePolicy;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 声誉记账核心：先查重（幂等）→ 改余额 → 追加流水 → 发布 ReputationChanged。
 * 余额与流水在同一事务中提交，保证“余额 = 流水之和”。
 */
@Service
public class ReputationLedgerService {

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;
    private final BadgeService badges;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public ReputationLedgerService(ReputationAccountRepository accounts, LedgerRepository ledger, BadgeService badges,
                                   IntegrationEventPublisher events, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.badges = badges;
        this.events = events;
        this.clock = clock;
    }

    /** @return true 表示本次确实记账；false 表示重复事件已被忽略或增量为 0。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean record(long userId, String sourceEventId, ReputationReason reason, int delta, String refType,
                          long refId) {
        if (delta == 0 || ledger.exists(sourceEventId, userId, reason)) {
            return false;
        }
        ReputationAccount account = accounts.findByUserId(userId).orElseGet(() -> ReputationAccount.open(userId, 0));
        account.applyReputation(delta);
        accounts.save(account);
        ledger.append(LedgerEntry.of(userId, sourceEventId, reason, delta, refType, refId, clock.instant()));
        events.publish(new ReputationChangedEvent(EventIds.next(), clock.instant(), userId, delta,
                account.reputation(), reason.name()));
        BadgePolicy.forReputation(account.reputation()).forEach(b -> badges.grant(userId, b));
        return true;
    }
}

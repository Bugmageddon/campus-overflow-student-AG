package com.campusoverflow.reputation.infrastructure.persistence;

import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.Bounty;
import com.campusoverflow.reputation.domain.BountyRepository;
import com.campusoverflow.reputation.domain.BountyStatus;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

/**
 * Reputation 上下文的持久化适配器（一个类实现多个仓储端口，演示“适配器粒度”可按上下文权衡）。
 */
@Repository
public class ReputationPersistenceAdapter
        implements ReputationAccountRepository, LedgerRepository, BountyRepository, BadgeRepository {

    private final AccountJpaRepository accounts;
    private final LedgerJpaRepository ledger;
    private final BountyJpaRepository bounties;
    private final BadgeJpaRepository badges;

    public ReputationPersistenceAdapter(AccountJpaRepository accounts, LedgerJpaRepository ledger,
                                        BountyJpaRepository bounties, BadgeJpaRepository badges) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.bounties = bounties;
        this.badges = badges;
    }

    // ---------------- ReputationAccountRepository ----------------

    @Override
    public Optional<ReputationAccount> findByUserId(long userId) {
        return accounts.findById(userId).map(ReputationPersistenceAdapter::toDomain);
    }

    @Override
    public List<ReputationAccount> findAllByUserId(Collection<Long> userIds) {
        return accounts.findAllById(userIds).stream().map(ReputationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ReputationAccount> topByReputation(int limit) {
        return accounts.findTop(org.springframework.data.domain.PageRequest.of(0, limit)).stream()
                .map(ReputationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ReputationAccount> findAll() {
        return accounts.findAll().stream().map(ReputationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public ReputationAccount save(ReputationAccount a) {
        AccountJpaEntity e = accounts.findById(a.userId()).orElseGet(() -> new AccountJpaEntity(a.userId()));
        e.setReputation(a.reputation());
        e.setAvailablePoints(a.availablePoints());
        e.setFrozenPoints(a.frozenPoints());
        accounts.save(e);
        return a;
    }

    private static ReputationAccount toDomain(AccountJpaEntity e) {
        return ReputationAccount.reconstitute(e.getUserId(), e.getReputation(), e.getAvailablePoints(),
                e.getFrozenPoints(), e.getVersion());
    }

    // ---------------- LedgerRepository ----------------

    @Override
    public boolean exists(String eventId, long userId, ReputationReason reason) {
        return ledger.existsByEventIdAndUserIdAndReason(eventId, userId, reason.name());
    }

    @Override
    public void append(LedgerEntry entry) {
        ledger.save(new LedgerJpaEntity(entry.userId(), entry.eventId(), entry.reason().name(), entry.delta(),
                entry.refType(), entry.refId(), entry.createdAt()));
    }

    @Override
    public PageResult<LedgerEntry> findByUser(long userId, PageRequest page) {
        Page<LedgerJpaEntity> p = ledger.findByUserIdOrderByIdDesc(userId,
                org.springframework.data.domain.PageRequest.of(page.page() - 1, page.size()));
        return new PageResult<>(p.getContent().stream()
                .map(e -> new LedgerEntry(e.getId(), e.getUserId(), e.getEventId(),
                        ReputationReason.valueOf(e.getReason()), e.getDelta(), e.getRefType(), e.getRefId(),
                        e.getCreatedAt()))
                .toList(), p.getTotalElements(), page.page(), page.size());
    }

    @Override
    public long countByUserAndReason(long userId, ReputationReason reason) {
        return ledger.countByUserIdAndReason(userId, reason.name());
    }

    @Override
    public int sumPositiveVoteDeltaSince(long userId, Instant since) {
        Long sum = ledger.sumPositiveSince(userId,
                List.of(ReputationReason.QUESTION_VOTE.name(), ReputationReason.ANSWER_VOTE.name()), since);
        return sum == null ? 0 : sum.intValue();
    }

    @Override
    public Map<Long, Long> sumByUser() {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : ledger.sumGroupByUser()) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    // ---------------- BountyRepository ----------------

    @Override
    public Optional<Bounty> findOpenByQuestion(long questionId) {
        return bounties.findFirstByQuestionIdAndStatus(questionId, BountyStatus.OPEN.name())
                .map(ReputationPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Bounty> findLatestByQuestion(long questionId) {
        return bounties.findFirstByQuestionIdOrderByIdDesc(questionId).map(ReputationPersistenceAdapter::toDomain);
    }

    @Override
    public List<Bounty> findDue(Instant now, int limit) {
        return bounties.findByStatusAndExpiresAtLessThanEqualOrderByExpiresAt(BountyStatus.OPEN.name(), now,
                        org.springframework.data.domain.PageRequest.of(0, limit)).stream()
                .map(ReputationPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Bounty save(Bounty b) {
        BountyJpaEntity e = b.id() == null
                ? new BountyJpaEntity(b.questionId(), b.sponsorId(), b.points(), b.createdAt(), b.expiresAt())
                : bounties.findById(b.id()).orElseThrow();
        e.setStatus(b.status().name());
        e.setWinnerId(b.winnerId());
        e.setAnswerId(b.answerId());
        e.setClosedAt(b.closedAt());
        BountyJpaEntity saved = bounties.save(e);
        if (b.id() == null) {
            b.assignId(saved.getId());
        }
        return b;
    }

    private static Bounty toDomain(BountyJpaEntity e) {
        return Bounty.reconstitute(e.getId(), e.getQuestionId(), e.getSponsorId(), e.getPoints(),
                BountyStatus.valueOf(e.getStatus()), e.getCreatedAt(), e.getExpiresAt(), e.getWinnerId(),
                e.getAnswerId(), e.getClosedAt());
    }

    // ---------------- BadgeRepository ----------------

    @Override
    public boolean has(long userId, BadgeType type) {
        return badges.existsByUserIdAndBadgeCode(userId, type.name());
    }

    @Override
    public void award(BadgeAward award) {
        badges.save(new BadgeJpaEntity(award.userId(), award.type().name(), award.awardedAt()));
    }

    @Override
    public List<BadgeAward> findByUser(long userId) {
        return badges.findByUserIdOrderByAwardedAt(userId).stream()
                .map(e -> new BadgeAward(e.getUserId(), BadgeType.valueOf(e.getBadgeCode()), e.getAwardedAt()))
                .toList();
    }
}

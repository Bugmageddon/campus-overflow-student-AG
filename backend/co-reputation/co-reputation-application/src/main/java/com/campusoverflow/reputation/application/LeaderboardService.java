package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 排行榜：周/月/学期榜来自 Redis ZSET，总榜直接读账户表。 */
@Service
public class LeaderboardService {

    private final LeaderboardStore store;
    private final ReputationAccountRepository accounts;
    private final IdentityApi identity;
    private final ProcessedEventStore processed;
    private final Clock clock;

    public LeaderboardService(LeaderboardStore store, ReputationAccountRepository accounts, IdentityApi identity,
                              ProcessedEventStore processed, Clock clock) {
        this.store = store;
        this.accounts = accounts;
        this.identity = identity;
        this.processed = processed;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(ReputationChangedEvent e) {
        if (!processed.markProcessed(e.eventId(), "reputation.leaderboard")) {
            return;
        }
        store.increment(e.userId(), e.delta(), e.occurredAt());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryView> top(LeaderboardPeriod period, int limit) {
        if (period == LeaderboardPeriod.ALL) {
            List<com.campusoverflow.reputation.domain.ReputationAccount> all = accounts.topByReputation(limit);
            List<Long> ids = all.stream().map(com.campusoverflow.reputation.domain.ReputationAccount::userId).toList();
            Map<Long, UserSummary> users = identity.findUsers(ids);
            List<LeaderboardEntryView> result = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                ReputationAccount a = all.get(i);
                UserSummary u = users.get(a.userId());
                result.add(new LeaderboardEntryView(i + 1, a.userId(), u == null ? "某同学" : u.displayName(),
                        u != null && u.verified(), a.reputation()));
            }
            return result;
        }
        List<LeaderboardStore.Entry> entries = store.top(period, clock.instant(), limit);
        List<Long> ids = entries.stream().map(LeaderboardStore.Entry::userId).toList();
        Map<Long, UserSummary> users = identity.findUsers(ids);
        List<LeaderboardEntryView> result = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardStore.Entry entry = entries.get(i);
            UserSummary u = users.get(entry.userId());
            result.add(new LeaderboardEntryView(i + 1, entry.userId(), u == null ? "某同学" : u.displayName(),
                    u != null && u.verified(), entry.score()));
        }
        return result;
    }
}
